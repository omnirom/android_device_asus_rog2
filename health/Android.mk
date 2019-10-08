LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := android.hardware.health@2.0-service.oneplus7pro
LOCAL_MODULE_TAGS  := optional

LOCAL_MODULE_PATH := $(TARGET_OUT_PRODUCT)/vendor_overlay/29/bin
LOCAL_MODULE_RELATIVE_PATH := hw
LOCAL_MODULE_STEM := android.hardware.health@2.0-service

LOCAL_SRC_FILES := \
    HealthServiceDefault.cpp

LOCAL_REQUIRED_MODULES := \
    android.hardware.health@2.0-service.oneplus7pro.rc

LOCAL_STATIC_LIBRARIES := \
    android.hardware.health@2.0-impl \
    android.hardware.health@1.0-convert \
    libhealthservice \
    libhealthstoragedefault \
    libbatterymonitor

LOCAL_SHARED_LIBRARIES := \
    libbase \
    libcutils \
    libhidlbase \
    libhidltransport \
    libhwbinder \
    liblog \
    libutils \
    android.hardware.health@2.0

include $(BUILD_EXECUTABLE)

include $(CLEAR_VARS)

LOCAL_MODULE := android.hardware.health@2.0-service.oneplus7pro.rc
LOCAL_MODULE_TAGS  := optional
LOCAL_MODULE_CLASS := ETC

LOCAL_MODULE_PATH := $(TARGET_OUT_PRODUCT)/vendor_overlay/29/etc/init
LOCAL_MODULE_STEM := android.hardware.health@2.0-service.rc

LOCAL_SRC_FILES := android.hardware.health@2.0-service.oneplus7pro.rc

include $(BUILD_PREBUILT)

include $(CLEAR_VARS)
LOCAL_SRC_FILES := healthd_board_msm.cpp healthd_msm_alarm.cpp
LOCAL_MODULE := libhealthd.msm

LOCAL_CFLAGS := -Werror
LOCAL_C_INCLUDES := \
    system/core/healthd/include/healthd/ \
    system/core/base/include \
    bootable/recovery \
    bootable/recovery/minui/include

LOCAL_HEADER_LIBRARIES := libbatteryservice_headers

include $(BUILD_STATIC_LIBRARY)