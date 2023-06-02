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
	mkdir -p ../release/mac/
	cp $(BUILD_DIR)/* ../release/mac/
else
	mkdir -p ../release/linux/
	cp $(BUILD_DIR)/* ../release/linux/
endif

clean:
	$(MAKE) -C common clean
	$(MAKE) -C faiss clean
	$(MAKE) -C hnsw clean
	rm -rf $(BUILD_DIR)
