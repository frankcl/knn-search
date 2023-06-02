PLATFORM = $(shell uname -s)
ifeq (,$(filter Darwin Linux,$(PLATFORM)))
	$(error unsupported platform[$(PLATFORM)])
endif

include ../../make_common.dep

INCLUDE_OPTIONS = -I../../../
LINK_LIBS = -lstdc++ -lgtest -lgtest_main -lpthread -ltcmalloc
SOURCE = $(wildcard ./*cpp)
TARGET = knn_common_test

$(TARGET):$(SOURCE)
	$(COMPILER) $(SOURCE) -o $(BUILD_DIR)/$(TARGET) $(COMPILE_OPTIONS) $(INCLUDE_OPTIONS) $(LINK_LIBS)
ifeq ($(PLATFORM),Darwin)
	rm -fr $(BUILD_DIR)/*.dSYM
endif

all: $(TARGET)

clean:
	rm -rf $(BUILD_DIR)

test:
ifeq ($(PLATFORM),Darwin)
	HEAPCHECK=normal $(BUILD_DIR)/$(TARGET)
else
	HEAPCHECK=normal $(BUILD_DIR)/$(TARGET)
endif
