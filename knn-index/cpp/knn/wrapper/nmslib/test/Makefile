PLATFORM = $(shell uname -s)
ifeq (,$(filter Darwin Linux,$(PLATFORM)))
	$(error unsupported platform[$(PLATFORM)])
endif

include ../../../make_common.dep

LINK_OPTIONS = -L$(NMSLIB_DIR)/release -L../../../jni/common/build
LINK_LIBS = -lstdc++ -lKNNCommon -lNonMetricSpaceLib -lm -lgtest -lgtest_main -lpthread -ltcmalloc
ifeq ($(PLATFORM),Darwin)
    INCLUDE_OPTIONS = -I$(NMSLIB_DIR)/include -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/darwin -I../../../..
else
    INCLUDE_OPTIONS = -I$(NMSLIB_DIR)/include -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux -I../../../..
endif
SOURCE = $(wildcard ./*cpp ../*cpp)
TARGET = knn_nmslib_wrapper_test

$(TARGET):$(SOURCE)
	$(COMPILER) $(SOURCE) -o $(BUILD_DIR)/$(TARGET) $(COMPILE_OPTIONS) $(LINK_OPTIONS) $(INCLUDE_OPTIONS) $(LINK_LIBS)
ifeq ($(PLATFORM),Darwin)
	rm -fr $(BUILD_DIR)/*.dSYM
endif

all: $(TARGET)

clean:
	rm -rf $(BUILD_DIR)

test:
ifeq ($(PLATFORM),Darwin)
	DYLD_LIBRARY_PATH=../../../jni/common/build HEAPCHECK=normal $(BUILD_DIR)/$(TARGET)
else
	LD_LIBRARY_PATH=../../../jni/common/build HEAPCHECK=normal $(BUILD_DIR)/$(TARGET)
endif
