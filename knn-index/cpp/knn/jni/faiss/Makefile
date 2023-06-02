PLATFORM = $(shell uname -s)
ifeq (,$(filter Darwin Linux,$(PLATFORM)))
	$(error unsupported platform[$(PLATFORM)])
endif

include ../../make_common.dep

ifeq ($(PLATFORM),Darwin)
	LINK_LIBS = -lstdc++ -lKNNCommon -lfaiss -lomp
	INCLUDE_OPTIONS = -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/darwin -I$(MAC_OMP_INCLUDE_OPTION) -I$(CURDIR)/../../..
	LINK_OPTIONS = -L$(MAC_OMP_LINK_OPTION) -L$(CURDIR)/../common/build
	SHARED_TARGET = libFAISSIndexJNI_$(FAISS_VERSION).dylib
else
	LINK_LIBS = -lstdc++ -lKNNCommon -lfaiss
	INCLUDE_OPTIONS = -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux -I$(CURDIR)/../../..
	LINK_OPTIONS = -L$(CURDIR)/../common/build
	SHARED_TARGET = libFAISSIndexJNI_$(FAISS_VERSION).so
endif
SOURCE = $(wildcard ./*cpp ../../wrapper/faiss/*cpp)

$(SHARED_TARGET):$(SOURCE)
	$(COMPILER) $(SOURCE) -shared -fPIC -o $(SHARED_TARGET) $(COMPILE_OPTIONS) $(LINK_OPTIONS) $(INCLUDE_OPTIONS) $(LINK_LIBS)
	mv $(SHARED_TARGET) $(BUILD_DIR)
ifeq ($(PLATFORM),Darwin)
	rm -fr *.dSYM
endif

all: $(SHARED_TARGET)

clean:
	rm -rf $(BUILD_DIR)
