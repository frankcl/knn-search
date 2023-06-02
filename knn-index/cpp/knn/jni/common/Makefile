PLATFORM = $(shell uname -s)
ifeq (,$(filter Darwin Linux,$(PLATFORM)))
	$(error unsupported platform[$(PLATFORM)])
endif

include ../../make_common.dep

LINK_LIBS = -lstdc++
ifeq ($(PLATFORM),Darwin)
	INCLUDE_OPTIONS = -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/darwin -I$(CURDIR)/../../..
	SHARED_TARGET = libKNNCommon.dylib
else
	INCLUDE_OPTIONS = -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux -I$(CURDIR)/../../..
	SHARED_TARGET = libKNNCommon.so
endif
SOURCE = $(wildcard ./*cpp)

$(SHARED_TARGET):$(SOURCE)
	$(COMPILER) *.cpp -shared -fPIC -o $(SHARED_TARGET) $(COMPILE_OPTIONS) $(INCLUDE_OPTIONS) $(LINK_LIBS)
	mv $(SHARED_TARGET) $(BUILD_DIR)
ifeq ($(PLATFORM),Darwin)
	rm -fr *.dSYM
endif

all: $(SHARED_TARGET)

clean:
	rm -rf $(BUILD_DIR)
