PLATFORM = $(shell uname -s)
ifeq (,$(filter Darwin Linux,$(PLATFORM)))
	$(error unsupported platform[$(PLATFORM)])
endif

include ../make_common.dep

all:
	$(MAKE) -C common
	$(MAKE) -C faiss
	$(MAKE) -C hnsw
	cp common/$(BUILD_DIR)/* $(BUILD_DIR)
	cp hnsw/$(BUILD_DIR)/* $(BUILD_DIR)
	cp faiss/$(BUILD_DIR)/* $(BUILD_DIR)

install:
ifeq ($(PLATFORM),Darwin)
	cp $(BUILD_DIR)/* ../release/mac/
else
	cp $(BUILD_DIR)/* ../release/linux/
endif

clean:
	$(MAKE) -C common clean
	$(MAKE) -C faiss clean
	$(MAKE) -C hnsw clean
	rm -rf $(BUILD_DIR)
