LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_CFLAGS    := -Wno-write-strings -DANDROID -DFRODO_PC -DPRECISE_CPU_CYCLES=1 -DPRECISE_CIA_CYCLES=1 -DPC_IS_POINTER=0
LOCAL_MODULE    := Droid64
LOCAL_SRC_FILES := $(notdir $(wildcard $(LOCAL_PATH)/*.cpp)) \
				   $(addprefix rom/,$(notdir $(wildcard $(LOCAL_PATH)/rom/*.cpp))) \
				   $(addprefix emu/,$(notdir $(wildcard $(LOCAL_PATH)/emu/*.cpp))) \
				   $(addprefix emu/pc/,$(notdir $(wildcard $(LOCAL_PATH)/emu/pc/*.cpp)))

include $(BUILD_SHARED_LIBRARY)
