ARCH = $(shell uname -s)

CURRENT_DIR = $(CURDIR)
VERSION = V1_7_3
CC = gcc
CFLAGS = -g -O2 -Wall -std=c++11
LIBS = -lstdc++ -lKNNCommon -lfaiss -lomp

JAVA_HOME = /Library/Java/JavaVirtualMachines/jdk-11.0.16.1.jdk/Contents/Home/
JNI_BASE_INCLUDE = $(JAVA_HOME)include
JNI_PLATFORM_INCLUDE = $(JAVA_HOME)include/darwin
INCLUDE_DIR = -I$(JNI_BASE_INCLUDE) -I$(JNI_PLATFORM_INCLUDE) -I$(CURRENT_DIR)/../../..
LIBS_DIR = -L$(CURRENT_DIR)/../common/build
BUILD_DIR = ./build/
SOURCE = $(wildcard ./*cpp ../../wrapper/faiss/*cpp)

ifeq ($(ARCH),Darwin)
	SHARED_TARGET = libFAISSIndexJNI_$(VERSION).dylib
	INCLUDE_DIR = -I$(JNI_BASE_INCLUDE) -I$(JNI_PLATFORM_INCLUDE) -I/opt/homebrew/opt/libomp/include -I$(CURRENT_DIR)/../../..
	LIBS_DIR = -L/opt/homebrew/opt/libomp/lib -L$(CURRENT_DIR)/../common/build
else
	SHARED_TARGET = libFAISSIndexJNI_$(VERSION).so
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
