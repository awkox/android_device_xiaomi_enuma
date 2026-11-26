#
# SPDX-FileCopyrightText: The LineageOS Project
# SPDX-License-Identifier: Apache-2.0
#

# A/B
TARGET_IS_VAB := true

# No vibrator motor on elish
TARGET_DISABLE_VIBRATOR := true

# Is tablet
TARGET_IS_TABLET := true

# Inherit from sm8250-common
$(call inherit-product, device/xiaomi/sm8250-common/kona.mk)

# No vibrator motor on elish
PRODUCT_COPY_FILES += \
    $(LOCAL_PATH)/unavailable-features.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/unavailable-features.xml

# AAPT
PRODUCT_AAPT_CONFIG := normal
PRODUCT_AAPT_PREF_CONFIG := xxxhdpi
PRODUCT_AAPT_PREBUILT_DPI := xxxhdpi xxhdpi xhdpi hdpi

# Audio configs
PRODUCT_COPY_FILES += \
    $(call find-copy-subdir-files,*,$(LOCAL_PATH)/audio/,$(TARGET_COPY_OUT_VENDOR)/etc)

# Boot animation
TARGET_SCREEN_HEIGHT := 2560
TARGET_SCREEN_WIDTH := 1600

# Camera
PRODUCT_PACKAGES += \
    libMegviiFacepp-0.5.2 \
    libmegface \
    libpiex_shim

# Fingerprint
TARGET_SUPPORTS_FINGERPRINT := true

# Parts
PRODUCT_PACKAGES += \
    MiPadParts

PRODUCT_PACKAGES += \
    vendor.xiaomi_elish.peripherals@1.0-service.default

$(call soong_config_set_bool, xiaomi_elish_peripherals, stylus_use_old_driver, true)

# Stylus button bridge (BLE keyboard -> BTN_STYLUS via uinput)
PRODUCT_PACKAGES += \
    stylus-bridge

# DSP Volume Synchronizer (syncs Android volume to Xiaomi DSP/Dolby)
PRODUCT_PACKAGES += \
    DSPVolumeSynchronizer

# Rootdir
PRODUCT_PACKAGES += \
    init.elish.rc

# RRO Overlays
PRODUCT_PACKAGES += \
    ApertureOverlayDevice \
    FrameworkResOverlayDevice \
    LineageSDKOverlayDevice \
    SettingsOverlayDevice \
    SettingsProviderOverlayDevice \
    SystemUIOverlayDevice

# Permissions
PRODUCT_COPY_FILES += \
    frameworks/native/data/etc/tablet_core_hardware.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/tablet_core_hardware.xml

# Shipping API level
PRODUCT_SHIPPING_API_LEVEL := 30

# Soong namespaces
PRODUCT_SOONG_NAMESPACES += \
    $(LOCAL_PATH)

# Inherit from vendor blobs
$(call inherit-product, vendor/xiaomi/elish/elish-vendor.mk)
