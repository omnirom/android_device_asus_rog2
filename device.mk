# Copyright (C) 2016 The CyanogenMod Project
# Copyright (C) 2019 The OmniRom Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

#
# This file is the build configuration for a full Android
# build for grouper hardware. This cleanly combines a set of
# device-specific aspects (drivers) with a device-agnostic
# product configuration (apps).
#
$(call inherit-product, vendor/asus/rog2/rog2-vendor.mk)
$(call inherit-product, vendor/images/asus/rog2/rog2-app.mk)

# Inherit from asus sm8150-common
$(call inherit-product, device/asus/sm8150-common/common.mk)

# Overlays
DEVICE_PACKAGE_OVERLAYS += \
    $(LOCAL_PATH)/overlay

PRODUCT_PACKAGES += \
    FrameworksResDeviceOverlay \
    TetheringConfigOverlay \
    WifiOverlay

# audio
PRODUCT_COPY_FILES += \
    $(LOCAL_PATH)/audio/audio_io_policy.conf:$(TARGET_COPY_OUT_VENDOR)/etc/audio_io_policy.conf \
    $(LOCAL_PATH)/audio/audio_io_policy_24bit.conf:$(TARGET_COPY_OUT_VENDOR)/etc/audio_io_policy_24bit.conf \
    $(LOCAL_PATH)/audio/audio_policy_configuration.xml:$(TARGET_COPY_OUT_VENDOR)/etc/audio/audio_policy_configuration.xml \
    $(LOCAL_PATH)/audio/audio_policy_configuration.xml:$(TARGET_COPY_OUT_VENDOR)/etc/audio_policy_configuration.xml \
    $(LOCAL_PATH)/audio/audio_policy_volumes_ZS660KL.xml:$(TARGET_COPY_OUT_VENDOR)/etc/audio_policy_volumes_ZS660KL.xml \
    $(LOCAL_PATH)/audio/audio_effects_ZS660KL.xml:$(TARGET_COPY_OUT_VENDOR)/etc/audio_effects_ZS660KL.xml \
    $(LOCAL_PATH)/audio/audio_effects_ZS660KL.xml:$(TARGET_COPY_OUT_VENDOR)/etc/audio_effects.xml

# DeviceParts
PRODUCT_PACKAGES += \
    DeviceParts

# Init
PRODUCT_PACKAGES += \
    libinit_rog2

# Biometrics
PRODUCT_PACKAGES += \
    android.hardware.biometrics.fingerprint@2.3-service.rog2

# Health
PRODUCT_PACKAGES += \
    android.hardware.health@2.1-impl \
    android.hardware.health@2.1-service

# Input
PRODUCT_COPY_FILES += \
    $(LOCAL_PATH)/idc/goodix_ts.idc:system/usr/idc/goodix_ts.idc \
    $(LOCAL_PATH)/idc/goodix_ts_station.idc:system/usr/idc/goodix_ts_station.idc \
    $(LOCAL_PATH)/keychars/goodix_ts.kcm:system/usr/keychars/goodix_ts.kcm \
    $(LOCAL_PATH)/keylayout/goodix_ts.kl:system/usr/keylayout/goodix_ts.kl

# Prebuilt
PRODUCT_COPY_FILES += \
    $(call find-copy-subdir-files,*,device/asus/rog2/prebuilt/system,system) \
    $(call find-copy-subdir-files,*,device/asus/rog2/prebuilt/recovery,recovery/root) \
    $(call find-copy-subdir-files,*,device/asus/rog2/prebuilt/vendor,vendor)

# Soong namespaces
PRODUCT_SOONG_NAMESPACES += \
    $(LOCAL_PATH)

# Vibrator
PRODUCT_PACKAGES += \
    android.hardware.vibrator@1.2-service.rog2
