PLATFORM = $(shell uname -s)
ifeq (,$(filter Darwin Linux,$(PLATFORM)))
	$(error unsupported platform[$(PLATFORM)])
endif

include ../../make_common.dep

LINK_LIBS = -lstdc++ -lKNNCommon -lNonMetricSpaceLib
LINK_OPTIONS = -L$(NMSLIB_DIR)/release -L$(CURDIR)/../common/build
ifeq ($(PLATFORM),Darwin)
    INCLUDE_OPTIONS = -I$(NMSLIB_DIR)/include -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/darwin -I$(CURDIR)/../../..
	SHARED_TARGET = libHNSWIndexJNI_$(NMSLIB_VERSION).dylib
else
	INCLUDE_OPTIONS = -I$(NMSLIB_DIR)/include -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux -I$(CURDIR)/../../..
	SHARED_TARGET = libHNSWIndexJNI_$(NMSLIB_VERSION).so
endif
SOURCE = $(wildcard ./*cpp ../../wrapper/nmslib/*cpp)

$(SHARED_TARGET):$(SOURCE)
	$(COMPILER) $(SOURCE) -shared -fPIC -o $(SHARED_TARGET) $(COMPILE_OPTIONS) $(LINK_OPTIONS) $(INCLUDE_OPTIONS) $(LINK_LIBS)
	mv $(SHARED_TARGET) $(BUILD_DIR)
ifeq ($(PLATFORM),Darwin)
	rm -fr *.dSYM
endif

all: $(SHARED_TARGET)

clean:
	rm -rf $(BUILD_DIR)