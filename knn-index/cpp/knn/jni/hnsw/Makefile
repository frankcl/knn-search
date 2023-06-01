NMSLIB_BASE_DIR = /Users/frankcl/Code/public/nmslib/2.1.1/nmslib-2.1.1/similarity_search/
#NMSLIB_BASE_DIR = /home/admin/tools/nmslib-2.1.1/similarity_search/
JAVA_HOME = /Library/Java/JavaVirtualMachines/jdk-11.0.16.1.jdk/Contents/Home/
#JAVA_HOME = /usr/lib/jvm/java/

ARCH = $(shell uname -s)
CURRENT_DIR = $(CURDIR)
VERSION = V2_1_1
CC = gcc
CFLAGS = -g -O2 -Wall -std=c++11
LIBS = -lstdc++ -lKNNCommon -lNonMetricSpaceLib
JNI_BASE_INCLUDE = $(JAVA_HOME)include
LIBS_DIR = -L$(NMSLIB_BASE_DIR)release -L$(CURRENT_DIR)/../common/build
BUILD_DIR = ./build/

ifeq ($(ARCH),Darwin)
	JNI_PLATFORM_INCLUDE = $(JAVA_HOME)include/darwin
	SHARED_TARGET = libHNSWIndexJNI_$(VERSION).dylib
else
	JNI_PLATFORM_INCLUDE = $(JAVA_HOME)include/linux
	SHARED_TARGET = libHNSWIndexJNI_$(VERSION).so
endif

INCLUDE_DIR = -I$(NMSLIB_BASE_DIR)include -I$(JNI_BASE_INCLUDE) -I$(JNI_PLATFORM_INCLUDE) -I$(CURRENT_DIR)/../../..

SOURCE = $(wildcard ./*cpp ../../wrapper/nmslib/*cpp)

$(shell mkdir -p $(BUILD_DIR))

$(SHARED_TARGET):$(SOURCE)
	$(CC) $(SOURCE) -shared -fPIC -o $(SHARED_TARGET) $(CFLAGS) $(LIBS_DIR) $(INCLUDE_DIR) $(LIBS)
	mv $(SHARED_TARGET) $(BUILD_DIR)
	rm -fr *.dSYM

all: $(SHARED_TARGET)

clean:
	rm -rf $(BUILD_DIR)$(SHARED_TARGET)
	rm -rf $(BUILD_DIR)
