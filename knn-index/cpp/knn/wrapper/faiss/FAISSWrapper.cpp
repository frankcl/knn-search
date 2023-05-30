#include <cstdio>
#include <memory>
#include <stdexcept>
#include <sstream>
#include <faiss/IndexIDMap.h>
#include <faiss/IndexIVF.h>
#include <faiss/IndexHNSW.h>
#include <faiss/index_factory.h>
#include <faiss/index_io.h>
#include "knn/common/StringUtil.h"
#include "knn/wrapper/faiss/FAISSConstants.h"
#include "knn/wrapper/faiss/FAISSWrapper.h"

using namespace std;
using namespace faiss;

BEGIN_NAMESPACE(xin)
BEGIN_NAMESPACE(manong)
BEGIN_NAMESPACE(knn)
BEGIN_NAMESPACE(wrapper)
BEGIN_NAMESPACE(faiss)

Index* FAISSWrapper::getInnerIndexImpl(Index* index) {
    if (index == nullptr) return nullptr;
    IndexIDMapTemplate<Index>* tpl = dynamic_cast<IndexIDMapTemplate<Index>*>(index);
    if (tpl != nullptr) return tpl->index;
    return index;
}

void FAISSWrapper::configIndex(Index* index, const unordered_map<string, string>& paramMap) {
    Index* innerIndex = getInnerIndexImpl(index);
    IndexIVF* indexIVF = dynamic_cast<IndexIVF*>(innerIndex);
    int32_t nprobe = 0, efSearch = 0, efConstruction = 0;
    if (indexIVF != nullptr && paramMap.find(FAISSConstants::PARAM_N_PROBE) != paramMap.end()) {
        string value = paramMap.at(FAISSConstants::PARAM_N_PROBE);
        if (common::StringUtil::strToInt32(value, nprobe) && nprobe > 0) indexIVF->nprobe = nprobe;
    }
    IndexHNSW* indexHNSW = dynamic_cast<IndexHNSW*>(innerIndex);
    if (indexHNSW != nullptr) {
        if (paramMap.find(FAISSConstants::PARAM_EF_CONSTRUCTION) != paramMap.end()) {
            string value = paramMap.at(FAISSConstants::PARAM_EF_CONSTRUCTION);
            if (common::StringUtil::strToInt32(value, efConstruction) && efConstruction > 0) {
                indexHNSW->hnsw.efConstruction = efConstruction;
            }
        }
        if (paramMap.find(FAISSConstants::PARAM_EF_SEARCH) != paramMap.end()) {
            string value = paramMap.at(FAISSConstants::PARAM_EF_SEARCH);
            if (common::StringUtil::strToInt32(value, efSearch) && efSearch > 0) {
                indexHNSW->hnsw.efSearch = efSearch;
            }
        }
    }
    if (paramMap.find(FAISSConstants::PARAM_INDEX_THREAD_QUANTITY) != paramMap.end()) {
        int32_t threadCount = 0;
        string value = paramMap.at(FAISSConstants::PARAM_INDEX_THREAD_QUANTITY);
        if (common::StringUtil::strToInt32(value, threadCount) && threadCount > 1) {
            omp_set_num_threads(threadCount);
        }
    }
}

void FAISSWrapper::dump(const string& indexDescription, const FAISSData& indexData,
                        const unordered_map<string, string>& paramMap, const string& path) {
    indexData.check();
    if (indexDescription.empty()) throw runtime_error("index description is empty");
    try {
        shared_ptr<Index> index(index_factory(indexData.dimension, indexDescription.c_str()));
        if (index.get() == nullptr) {
            stringstream ss;
            ss << "create index failed for description[" << indexDescription << "]";
            throw runtime_error(ss.str());
        }
        configIndex(index.get(), paramMap);
        index->train(indexData.size, indexData.data);
        index->add_with_ids(indexData.size, indexData.data, indexData.ids);
        write_index(index.get(), path.c_str());
    } catch (exception& e) {
        stringstream ss;
        ss << "create index[" << path << "] failed, message[" << e.what() << "]";
        throw runtime_error(ss.str());
    }
}

Index* FAISSWrapper::open(const string& path) {
    FILE* fp = fopen(path.c_str(), "r");
    if (fp == nullptr) throw runtime_error("open index file[" + path + "] failed");
    Index* index = nullptr;
    try {
        index = read_index(fp);
        fclose(fp);
        return index;
    } catch (exception& e) {
        fclose(fp);
        DELETE_AND_SET_NULL(index);
        stringstream ss;
        ss << "open index[" << path << "] failed, message[" << e.what() << "]";
        throw runtime_error(ss.str());
    }
}

void FAISSWrapper::close(Index* index) {
    DELETE_AND_SET_NULL(index);
}

FAISSResult* FAISSWrapper::search(const Index* index, const float* vector, const uint32_t k) {
    if (index == nullptr) throw runtime_error("search index is null ptr");
    if (vector == nullptr) throw runtime_error("search vector is null ptr");
    if (k <= 0) {
        stringstream ss;
        ss << "invalid k[" << k << "]";
        throw runtime_error(ss.str());
    }
    FAISSResult* result = nullptr;
    try {
        result = new FAISSResult(k);
        index->search(1, vector, k, result->distances, result->ids);
        for (uint32_t i = 0; i < k; i++) {
            if (result->ids[i] == -1) break;
            result->n = i + 1;
        }
        return result;
    } catch (exception& e) {
        DELETE_AND_SET_NULL(result);
        stringstream ss;
        ss << "search failed, message[" << e.what() << "]";
        throw runtime_error(ss.str());
    }
}

END_NAMESPACE(faiss)
END_NAMESPACE(wrapper)
END_NAMESPACE(knn)
END_NAMESPACE(manong)
END_NAMESPACE(xin)
