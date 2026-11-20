#
# Copyright (C) The LineageOS Project
#
# SPDX-License-Identifier: Apache-2.0
#

# A/B
TARGET_IS_VAB := true

# No vibrator motor on enuma
TARGET_DISABLE_VIBRATOR := true

# Is tablet (false because enuma has cellular modem)
TARGET_IS_TABLET := false

# Inherit from sm8250-common
$(call inherit-product, device/xiaomi/sm8250-common/kona.mk)

# No NFC on enuma
PRODUCT_PACKAGES_REMOVE += NfcNci

# No vibrator motor on enuma
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
PRODUCT_COPY_FILES += \
    $(LOCAL_PATH)/camera/camera_cnf.txt:$(TARGET_COPY_OUT_VENDOR)/etc/camera/camera_cnf.txt

PRODUCT_PACKAGES += \
    libpiex_shim

# Fingerprint
PRODUCT_PACKAGES += \
    vendor.xiaomi.hardware.fx.tunnel@1.0.vendor

# GNSS
PRODUCT_PACKAGES += \
    android.hardware.gnss@2.1.vendor

# Overlays
PRODUCT_PACKAGES += \
    ApertureOverlayDevice \
    FrameworkResOverlayDevice \
    LineageDialerOverlayDevice \
    LineageSettingsOverlayDevice \
    LineageSystemUIOverlayDevice \
    SystemUIOverlayDevice

# Shipping API level
PRODUCT_SHIPPING_API_LEVEL := 30

# Soong namespaces
PRODUCT_SOONG_NAMESPACES += \
    $(LOCAL_PATH)

# Inherit from vendor blobs
$(call inherit-product, vendor/xiaomi/enuma/enuma-vendor.mk)
