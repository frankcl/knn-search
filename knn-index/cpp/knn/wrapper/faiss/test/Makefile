PLATFORM = $(shell uname -s)
ifeq (,$(filter Darwin Linux,$(PLATFORM)))
	$(error unsupported platform[$(PLATFORM)])
endif

include ../../../make_common.dep

SOURCE = $(wildcard ./*cpp ../*cpp)
TARGET = knn_faiss_wrapper_test
ifeq ($(PLATFORM),Darwin)
    INCLUDE_OPTIONS = -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/darwin -I$(MAC_OMP_INCLUDE_OPTION) -I../../../..
    LINK_OPTIONS = -L$(MAC_OMP_LINK_OPTION) -L../../../jni/common/build
    LINK_LIBS = -lstdc++ -lKNNCommon -lfaiss -lomp -lgtest -lgtest_main -lpthread -ltcmalloc
else
    COMPILE_OPTIONS = -g -O2 -Wall -std=c++11 -fopenmp
    INCLUDE_OPTIONS = -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux -I../../../..
    LINK_OPTIONS = -L../../../jni/common/build
    LINK_LIBS = -lstdc++ -lKNNCommon -lfaiss -lgtest -lgtest_main -lpthread -ltcmalloc
endif

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
