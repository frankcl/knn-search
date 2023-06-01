JAVA_HOME = /Library/Java/JavaVirtualMachines/jdk-11.0.16.1.jdk/Contents/Home/
#JAVA_HOME = /usr/lib/jvm/java/
NMSLIB_BASE_DIR = /Users/frankcl/Code/public/nmslib/2.1.1/nmslib-2.1.1/similarity_search/
#NMSLIB_BASE_DIR = /home/admin/tools/nmslib-2.1.1/similarity_search/

ARCH = $(shell uname -s)
CC = gcc
CFLAGS = -g -O2 -Wall -std=c++14
BUILD_DIR = ./build/
LIBS_DIR = -L$(NMSLIB_BASE_DIR)release -L../../../jni/common/build
JNI_BASE_INCLUDE = $(JAVA_HOME)include
LIBS = -lstdc++ -lKNNCommon -lNonMetricSpaceLib -lm -lgtest -lgtest_main -lpthread -ltcmalloc

ifeq ($(ARCH),Darwin)
    JNI_PLATFORM_INCLUDE = $(JAVA_HOME)include/darwin
else
    JNI_PLATFORM_INCLUDE = $(JAVA_HOME)include/linux
endif

INCLUDE_DIR = -I$(NMSLIB_BASE_DIR)include -I$(JNI_BASE_INCLUDE) -I$(JNI_PLATFORM_INCLUDE) -I../../../..

SOURCE = $(wildcard ./*cpp ../*cpp)
TARGET = knn_nmslib_wrapper_test

$(shell mkdir -p $(BUILD_DIR))

$(TARGET):$(SOURCE)
	$(CC) $(SOURCE) -o $(BUILD_DIR)$(TARGET) $(CFLAGS) $(LIBS_DIR) $(INCLUDE_DIR) $(LIBS)
	rm -fr $(BUILD_DIR)*.dSYM

all: $(TARGET)

clean:
	rm -rf $(BUILD_DIR)$(TARGET)
	rm -rf $(BUILD_DIR)

runtest:
ifeq ($(ARCH),Darwin)
	DYLD_LIBRARY_PATH=/usr/local/lib:../../../jni/common/build HEAPCHECK=normal $(BUILD_DIR)$(TARGET)
else
	LD_LIBRARY_PATH=/usr/local/lib:../../../jni/common/build HEAPCHECK=normal $(BUILD_DIR)$(TARGET)
endif
