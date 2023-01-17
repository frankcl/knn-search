CC = gcc
CFLAGS = -g -O2 -Wall -std=c++14
LIBS = -lstdc++ -lgtest -lgtest_main -lpthread -ltcmalloc

BUILD_DIR = ./build/
INCLUDE_DIR = -I../../../
SOURCE = $(wildcard ./*cpp)
TARGET = knn_common_test

ARCH = $(shell uname -s)

$(shell mkdir -p $(BUILD_DIR))

$(TARGET):$(SOURCE)
	$(CC) $(SOURCE) -o $(BUILD_DIR)$(TARGET) $(CFLAGS) $(INCLUDE_DIR) $(LIBS)
	rm -fr $(BUILD_DIR)*.dSYM

all: $(TARGET)

clean:
	rm -rf $(BUILD_DIR)$(TARGET)
	rm -rf $(BUILD_DIR)

runtest:
ifeq ($(ARCH),Darwin)
	DYLD_LIBRARY_PATH=/usr/local/lib HEAPCHECK=normal $(BUILD_DIR)$(TARGET)
else
	LD_LIBRARY_PATH=/usr/local/lib HEAPCHECK=normal $(BUILD_DIR)$(TARGET)
endif
