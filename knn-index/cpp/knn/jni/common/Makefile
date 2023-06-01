JAVA_HOME = /Library/Java/JavaVirtualMachines/jdk-11.0.16.1.jdk/Contents/Home/
#JAVA_HOME = /usr/lib/jvm/java/

ARCH = $(shell uname -s)
CURRENT_DIR = $(CURDIR)
CC = gcc
CFLAGS = -g -O2 -Wall -std=c++11
LIBS = -lstdc++
JNI_BASE_INCLUDE = $(JAVA_HOME)include

ifeq ($(ARCH),Darwin)
	JNI_PLATFORM_INCLUDE = $(JAVA_HOME)include/darwin
	SHARED_TARGET = libKNNCommon.dylib
else
	JNI_PLATFORM_INCLUDE = $(JAVA_HOME)include/linux
	SHARED_TARGET = libKNNCommon.so
endif

INCLUDE_DIR = -I$(JNI_BASE_INCLUDE) -I$(JNI_PLATFORM_INCLUDE) -I$(CURRENT_DIR)/../../..
BUILD_DIR = ./build/
SOURCE = $(wildcard ./*cpp)

$(shell mkdir -p $(BUILD_DIR))

$(SHARED_TARGET):$(SOURCE)
	$(CC) *.cpp -shared -fPIC -o $(SHARED_TARGET) $(CFLAGS) $(INCLUDE_DIR) $(LIBS)
	mv $(SHARED_TARGET) $(BUILD_DIR)
	rm -fr *.dSYM

all: $(SHARED_TARGET)

clean:
	rm -rf $(BUILD_DIR)$(SHARED_TARGET)
	rm -rf $(BUILD_DIR)
