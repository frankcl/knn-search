ARCH = $(shell uname -s)

CURRENT_DIR = $(CURDIR)
CC = gcc
CFLAGS = -g -O2 -Wall -std=c++11
LIBS = -lstdc++

JAVA_HOME = /Library/Java/JavaVirtualMachines/jdk1.8.0_341.jdk/Contents/Home/
JNI_BASE_INCLUDE = $(JAVA_HOME)include
JNI_PLATFORM_INCLUDE = $(JAVA_HOME)include/darwin
INCLUDE_DIR = -I$(JNI_BASE_INCLUDE) -I$(JNI_PLATFORM_INCLUDE) -I$(CURRENT_DIR)/../../..
BUILD_DIR = ./build/
SOURCE = $(wildcard ./*cpp)

ifeq ($(ARCH),Darwin)
	SHARED_TARGET = libKNNCommon.dylib
else
	SHARED_TARGET = libKNNCommon.so
endif

$(shell mkdir -p $(BUILD_DIR))

$(SHARED_TARGET):$(SOURCE)
	$(CC) *.cpp -shared -fPIC -o $(SHARED_TARGET) $(CFLAGS) $(INCLUDE_DIR) $(LIBS)
	mv $(SHARED_TARGET) $(BUILD_DIR)
	rm -fr *.dSYM

all: $(SHARED_TARGET)

clean:
	rm -rf $(BUILD_DIR)$(SHARED_TARGET)
	rm -rf $(BUILD_DIR)