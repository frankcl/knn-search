ARCH = $(shell uname -s)

CURRENT_DIR = $(CURDIR)
VERSION = V2_1_1
CC = gcc
CFLAGS = -g -O2 -Wall -std=c++11
LIBS = -lstdc++ -lKNNCommon -lNonMetricSpaceLib

JAVA_HOME = /Library/Java/JavaVirtualMachines/jdk-11.0.16.1.jdk/Contents/Home/
JNI_BASE_INCLUDE = $(JAVA_HOME)include
JNI_PLATFORM_INCLUDE = $(JAVA_HOME)include/darwin
NMSLIB_BASE_DIR = /Users/frankcl/Code/public/nmslib/2.1.1/nmslib-2.1.1/similarity_search/
INCLUDE_DIR = -I$(NMSLIB_BASE_DIR)include -I$(JNI_BASE_INCLUDE) -I$(JNI_PLATFORM_INCLUDE) -I$(CURRENT_DIR)/../../..
LIBS_DIR = -L$(NMSLIB_BASE_DIR)release -L$(CURRENT_DIR)/../common/build
BUILD_DIR = ./build/
SOURCE = $(wildcard ./*cpp ../../wrapper/nmslib/*cpp)

ifeq ($(ARCH),Darwin)
	SHARED_TARGET = libHNSWIndexJNI_$(VERSION).dylib
else
	SHARED_TARGET = libHNSWIndexJNI_$(VERSION).so
endif

$(shell mkdir -p $(BUILD_DIR))

$(SHARED_TARGET):$(SOURCE)
	$(CC) $(SOURCE) -shared -fPIC -o $(SHARED_TARGET) $(CFLAGS) $(LIBS_DIR) $(INCLUDE_DIR) $(LIBS)
	mv $(SHARED_TARGET) $(BUILD_DIR)
	rm -fr *.dSYM

all: $(SHARED_TARGET)

clean:
	rm -rf $(BUILD_DIR)$(SHARED_TARGET)
	rm -rf $(BUILD_DIR)
