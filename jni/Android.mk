#$(shell echo "test echo">$(LOCAL_PATH)/echo_file)

# Read Android.mk on each SubDirectory
#include $(call all-subdir-makefiles)

# ------------------------------------------------
# ------------------------------------------------
# ------------------------------------------------


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


# This makefile supplies the rules for building a library of JNI code for
# use by our example of how to bundle a shared library with an APK.

#LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
$(warning $($LOCAL_PATH))

LOCAL_MODULE_TAGS := optional user eng

# This is the target being built.
LOCAL_MODULE:= libNSUART

# All of the source files that we will compile.
LOCAL_SRC_FILES:= nsuart.cpp

# All of the shared libraries we link against.
LOCAL_SHARED_LIBRARIES := \
	libutils

# No static libraries.
LOCAL_STATIC_LIBRARIES :=

# Also need the JNI headers.
LOCAL_C_INCLUDES += \
	$(JNI_H_INCLUDE)

# No special compiler flags.
LOCAL_CFLAGS +=

# Don't prelink this library.  For more efficient code, you may want
# to add this library to the prelink map and set this to true. However,
# it's difficult to do this for applications that are not supplied as
# part of a system image.

LOCAL_PRELINK_MODULE := false
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)
# Add prebuilt libocr
include $(CLEAR_VARS)
