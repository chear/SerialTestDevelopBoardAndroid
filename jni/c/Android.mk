#$(warning *** start compile serial.c ***)
LOCAL_PATH := $(call my-dir)
# Print LOCAL_PATH
$(warning $($LOCAL_PATH))
include $(CLEAR_VARS)

LOCAL_MODULE    := serialtest

LOCAL_SRC_FILES := com_topeet_serialtest_serial.c

LOCAL_LDLIBS += -llog 

LOCAL_LDLIBS +=-lm

include $(BUILD_SHARED_LIBRARY)
#$(warning *** finished compile serial.c ***)