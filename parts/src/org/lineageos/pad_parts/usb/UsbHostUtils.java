/*
 * Copyright (C) The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.pad_parts.usb;

import android.os.SystemProperties;
import android.util.Log;

public class UsbHostUtils {

    private static final String TAG = "UsbHostUtils";
    private static final String PROP_USB_HOST = "persist.sys.usb.host.enabled";
    private static final String MODE_PATH = "/sys/bus/platform/devices/a800000.ssusb/mode";

    public static void applyUsbHostMode(boolean enabled) {
        try {
            SystemProperties.set(PROP_USB_HOST, enabled ? "1" : "0");
        } catch (Exception e) {
            Log.e(TAG, "Cannot set USB host property", e);
        }
    }

    /**
     * Reads the dwc3 mode node, which reports "peripheral" when VBUS is
     * active, "host" when ID is grounded and "none" otherwise. That is the
     * live controller state, so it is what the settings UI has to show.
     *
     * @return the mode value, or an empty string when it cannot be read
     */
    public static String getMode() {
        try (java.io.BufferedReader reader =
                new java.io.BufferedReader(new java.io.FileReader(MODE_PATH))) {
            String state = reader.readLine();
            return state != null ? state.trim() : "";
        } catch (Exception e) {
            Log.e(TAG, "Cannot read USB mode", e);
            return "";
        }
    }

    public static boolean isHostModeActive() {
        return "host".equals(getMode());
    }
}
