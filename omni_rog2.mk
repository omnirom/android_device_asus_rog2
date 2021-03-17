# Copyright (C) 2010 The Android Open Source Project
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

# Sample: This is where we'd set a backup provider if we had one
# $(call inherit-product, device/sample/products/backup_overlay.mk)
$(call inherit-product, $(SRC_TARGET_DIR)/product/core_64_bit.mk)

# Get the prebuilt list of APNs
$(call inherit-product, vendor/omni/config/gsm.mk)

# Inherit from the common Open Source product configuration
$(call inherit-product, $(SRC_TARGET_DIR)/product/aosp_base_telephony.mk)

# must be before including omni part
TARGET_BOOTANIMATION_SIZE := 1080p

# Inherit from our custom product configuration
$(call inherit-product, vendor/omni/config/common.mk)

# Inherit from hardware-specific part of the product configuration
$(call inherit-product, device/asus/rog2/device.mk)

# Discard inherited values and use our own instead.
PRODUCT_DEVICE := rog2
PRODUCT_NAME := omni_rog2
PRODUCT_BRAND := asus
PRODUCT_MODEL := ASUS_I001D
PRODUCT_MANUFACTURER := asus

PRODUCT_GMS_CLIENTID_BASE := android-asus

TARGET_DEVICE := WW_I001D
PRODUCT_SYSTEM_DEVICE := ASUS_I001_1
PRODUCT_SYSTEM_NAME := WW_I001D

PRODUCT_BUILD_PROP_OVERRIDES += \
    PRODUCT_DEVICE=ASUS_I001_1 \
    PRODUCT_NAME=WW_I001D \
    TARGET_DEVICE=ZS660KL

OMNI_PRODUCT_PROPERTIES += \
    ro.sf.lcd_density=420

# Build properties from stock
ASUS_BUILD_ID := QKQ1.190825.002
ASUS_BUILD_NUMBER := 17.0240.2012.65-0

VENDOR_RELEASE := 10/$(ASUS_BUILD_ID)/$(ASUS_BUILD_NUMBER):user/release-keys
BUILD_FINGERPRINT := asus/WW_I001D/ASUS_I001_1:$(VENDOR_RELEASE)

# Security patch level from stock
PLATFORM_SECURITY_PATCH_OVERRIDE := 2020-12-05
