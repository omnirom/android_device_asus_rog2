LOCAL_PATH := $(call my-dir)
ifeq ($(TARGET_DEVICE),$(filter $(TARGET_DEVICE),rog2))
include $(CLEAR_VARS)

include packages/apps/OmniLib/common.mk

include $(call all-makefiles-under,$(LOCAL_PATH))
endif
