CC = gcc
CFLAGS = -g -O2 -Wall -std=c++14
LIBS = -lstdc++ -lKNNCommon -lfaiss -lomp -lgtest -lgtest_main -lpthread -ltcmalloc

JAVA_HOME = /Library/Java/JavaVirtualMachines/jdk-11.0.16.1.jdk/Contents/Home/
JNI_BASE_INCLUDE = $(JAVA_HOME)include
JNI_PLATFORM_INCLUDE = $(JAVA_HOME)include/darwin
BUILD_DIR = ./build/
INCLUDE_DIR = -I$(JNI_BASE_INCLUDE) -I$(JNI_PLATFORM_INCLUDE) -I/opt/homebrew/opt/libomp/include -I../../../..
LIBS_DIR = -L/opt/homebrew/opt/libomp/lib -L../../../jni/common/build
SOURCE = $(wildcard ./*cpp ../*cpp)
TARGET = knn_faiss_wrapper_test

ARCH = $(shell uname -s)

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
