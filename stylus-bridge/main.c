/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

/*
 * Stylus Bridge - translates KEY_PAGEUP/KEY_PAGEDOWN from
 * Xiaomi Smart Pen BLE keyboard into BTN_STYLUS/BTN_STYLUS2
 * via uinput, so both pen event nodes fuse into one stylus.
 *
 * Device matched by vendor/product ID (not name).
 *
 * Uses inotify to wait for pen — zero CPU when pen absent.
 * read() blocks when pen idle — zero CPU when no buttons pressed.
 */

#include <errno.h>
#include <stdio.h>
#include <unistd.h>
#include <fcntl.h>
#include <string.h>
#include <signal.h>
#include <stdlib.h>
#include <dirent.h>
#include <poll.h>
#include <sys/inotify.h>
#include <linux/input.h>
#include <linux/uinput.h>

#define LOG_TAG "StylusBridge"
#include <log/log.h>

#define DEVICE_NAME       "xiaomi-stylus-bridge"
#define INPUT_DIR         "/dev/input"
#define MAX_PATH          256

/* Xiaomi Smart Pen BLE identifiers */
#define PEN_VENDOR     0x1915
#define PEN_PRODUCT_V1 0xEAEA
#define PEN_PRODUCT_V2 0x4D81

static void signal_handler(int sig) {
    ALOGE("Caught signal %d, exiting", sig);
    _exit(sig);
}

static int is_pen_device(int fd) {
    struct input_id id;
    if (ioctl(fd, EVIOCGID, &id) < 0)
        return 0;
    return id.vendor == PEN_VENDOR &&
           (id.product == PEN_PRODUCT_V1 || id.product == PEN_PRODUCT_V2);
}

static int try_open_pen(void) {
    DIR *dir = opendir(INPUT_DIR);
    if (!dir)
        return -1;

    struct dirent *ent;
    while ((ent = readdir(dir)) != NULL) {
        if (strncmp(ent->d_name, "event", 5) != 0)
            continue;

        char path[MAX_PATH];
        snprintf(path, sizeof(path), "%s/%s", INPUT_DIR, ent->d_name);

        int fd = open(path, O_RDONLY);
        if (fd < 0)
            continue;

        if (is_pen_device(fd)) {
            if (ioctl(fd, EVIOCGRAB, 1) != 0) {
                /*
                 * Without the grab the kernel keeps delivering the pen's raw
                 * KEY_PAGEUP/PAGEDOWN to the system, so bridging on top of
                 * it would make every press arrive twice. Skip this node and
                 * keep scanning: one physical pen can expose several event
                 * nodes and a later one may still be grabbable.
                 */
                ALOGE("EVIOCGRAB failed on %s: %s, trying next node",
                      path, strerror(errno));
                close(fd);
                continue;
            }
            closedir(dir);
            ALOGI("Found pen: %s (grabbed)", path);
            return fd;
        }
        close(fd);
    }
    closedir(dir);
    return -1;
}

static int wait_for_pen(void) {
    int fd = try_open_pen();
    if (fd >= 0)
        return fd;

    ALOGI("Waiting for pen (inotify)...");

    int ifd = inotify_init();
    if (ifd < 0) {
        ALOGE("inotify_init failed: %s", strerror(errno));
        return -1;
    }

    int wd = inotify_add_watch(ifd, INPUT_DIR, IN_CREATE);
    if (wd < 0) {
        ALOGE("inotify_add_watch failed: %s", strerror(errno));
        close(ifd);
        return -1;
    }

    char buf[512];
    struct pollfd pfd = { .fd = ifd, .events = POLLIN };
    while (1) {
        /* Poll with a timeout: a device that is already present raises no
         * IN_CREATE, yet its grab may still be failing, so retry either
         * way instead of blocking until the pen is replugged. */
        int pr = poll(&pfd, 1, 1000);
        if (pr < 0) {
            if (errno == EINTR)
                continue;
            break;
        }
        if (pr > 0) {
            int len = read(ifd, buf, sizeof(buf));
            if (len <= 0)
                break;
            usleep(200000);
        }

        fd = try_open_pen();
        if (fd >= 0) {
            inotify_rm_watch(ifd, wd);
            close(ifd);
            return fd;
        }
    }

    close(ifd);
    return -1;
}

static int create_uinput_device(void) {
    int fd = open("/dev/uinput", O_WRONLY | O_NONBLOCK);
    if (fd < 0) {
        ALOGE("Cannot open /dev/uinput: %s", strerror(errno));
        return -1;
    }

    ioctl(fd, UI_SET_EVBIT, EV_KEY);
    ioctl(fd, UI_SET_EVBIT, EV_SYN);
    ioctl(fd, UI_SET_EVBIT, EV_ABS);
    ioctl(fd, UI_SET_KEYBIT, BTN_STYLUS);
    ioctl(fd, UI_SET_KEYBIT, BTN_STYLUS2);
    ioctl(fd, UI_SET_ABSBIT, ABS_PRESSURE);

    struct uinput_user_dev udev;
    memset(&udev, 0, sizeof(udev));
    strncpy(udev.name, DEVICE_NAME, UINPUT_MAX_NAME_SIZE - 1);
    udev.id.bustype = BUS_VIRTUAL;
    udev.id.vendor  = PEN_VENDOR;
    udev.id.product = 0xEAEB;
    udev.id.version = 1;
    udev.absmin[ABS_PRESSURE] = 0;
    udev.absmax[ABS_PRESSURE] = 255;

    if (write(fd, &udev, sizeof(udev)) < 0) {
        ALOGE("write uinput_user_dev failed: %s", strerror(errno));
        close(fd);
        return -1;
    }

    if (ioctl(fd, UI_DEV_CREATE) < 0) {
        ALOGE("UI_DEV_CREATE failed: %s", strerror(errno));
        close(fd);
        return -1;
    }

    return fd;
}

static void send_event(int fd, unsigned short type, unsigned short code, int value) {
    struct input_event ev;
    memset(&ev, 0, sizeof(ev));
    ev.type  = type;
    ev.code  = code;
    ev.value = value;
    (void)write(fd, &ev, sizeof(ev));
}

int main(void) {
    signal(SIGSEGV, signal_handler);
    signal(SIGABRT, signal_handler);
    signal(SIGTERM, signal_handler);

    ALOGI("Starting (pid=%d)", getpid());

    while (1) {
        int evfd = wait_for_pen();
        if (evfd < 0) {
            ALOGE("Failed to find pen, retrying...");
            sleep(3);
            continue;
        }

        int uifd = create_uinput_device();
        if (uifd < 0) {
            close(evfd);
            ALOGE("uinput failed, retrying...");
            sleep(3);
            continue;
        }

        ALOGI("Bridging active");

        struct input_event ev;
        ssize_t n;
        while ((n = read(evfd, &ev, sizeof(ev))) == sizeof(ev)) {
            if (ev.type != EV_KEY)
                continue;

            int code = -1;
            if (ev.code == KEY_PAGEUP)
                code = BTN_STYLUS2;
            else if (ev.code == KEY_PAGEDOWN)
                code = BTN_STYLUS;

            if (code < 0)
                continue;

            send_event(uifd, EV_KEY, code, ev.value);
            send_event(uifd, EV_ABS, ABS_PRESSURE, ev.value ? 255 : 0);
            send_event(uifd, EV_SYN, SYN_REPORT, 0);
        }

        ALOGI("Read loop exited: n=%zd errno=%d (%s)", n, errno, strerror(errno));
        if (uifd >= 0) {
            ioctl(uifd, UI_DEV_DESTROY);
            close(uifd);
        }
        close(evfd);
    }

    return 0;
}
