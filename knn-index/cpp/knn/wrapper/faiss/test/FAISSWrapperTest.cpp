#include <cstdio>
#include <iostream>
#include <string>
#include <vector>
#include <gtest/gtest.h>
#include "knn/wrapper/faiss/FAISSConstants.h"
#include "knn/wrapper/faiss/FAISSWrapper.h"

using namespace std;
using namespace faiss;

BEGIN_NAMESPACE(xin)
BEGIN_NAMESPACE(manong)
BEGIN_NAMESPACE(knn)
BEGIN_NAMESPACE(wrapper)
BEGIN_NAMESPACE(faiss)

class FAISSWrapperSuite : public testing::Test {
public:
    static void SetUpTestCase() {
    }

protected:
    FAISSData indexData;

protected:
    void SetUp() {
        indexData.dimension = 3;
        indexData.size = 10;
        int64_t* ids = new int64_t[indexData.size];
        float* data = new float[indexData.size * indexData.dimension];
        indexData.ids = ids;
        indexData.data = data;
        for (uint32_t i = 0; i < indexData.size; i++) {
            ids[i] = (int64_t) i;
            for (uint32_t j = 0; j < indexData.dimension; j++) {
                size_t pos = i * indexData.dimension + j;
                data[pos] = i * 10.0f;
            }
        }
    }
};

TEST_F(FAISSWrapperSuite, testFlatIndex) {
    string indexFile = "./test_flat_index.fss";
    unordered_map<string, string> paramMap;
    FAISSWrapper::dump("IDMap,Flat", indexData, paramMap, indexFile);

    Index* index = FAISSWrapper::open(indexFile);
    EXPECT_TRUE(index != NULL);
    float* vector = new float[indexData.dimension];
    for (uint32_t i = 0; i < indexData.dimension; i++) vector[i] = 12.0f;
    FAISSResult* result = FAISSWrapper::search(index, vector, 3);
    EXPECT_TRUE(result != NULL);
    EXPECT_EQ(1, result->ids[0]);
    EXPECT_EQ(2, result->ids[1]);
    EXPECT_EQ(0, result->ids[2]);
    EXPECT_EQ(12.0f, result->distances[0]);
    EXPECT_EQ(192.0f, result->distances[1]);
    EXPECT_EQ(432.0f, result->distances[2]);
    DELETE_AND_SET_NULL(result);
    DELETE_AND_SET_NULL(index);
    remove(indexFile.c_str());
}

TEST_F(FAISSWrapperSuite, testIVFFlatIndex) {
    string indexFile = "./test_ivf_flat_index.dat";
    unordered_map<string, string> paramMap;
    paramMap[FAISSConstants::PARAM_N_PROBE] = "2";
    FAISSWrapper::dump("IVF2,Flat", indexData, paramMap, indexFile);

    Index* index = FAISSWrapper::open(indexFile);
    EXPECT_TRUE(index != NULL);
    float* vector = new float[indexData.dimension];
    for (uint32_t i = 0; i < indexData.dimension; i++) vector[i] = 12.0f;
    FAISSResult* result = FAISSWrapper::search(index, vector, 3);
    EXPECT_TRUE(result != NULL);
    EXPECT_EQ(1, result->ids[0]);
    EXPECT_EQ(2, result->ids[1]);
    EXPECT_EQ(0, result->ids[2]);
    EXPECT_EQ(12.0f, result->distances[0]);
    EXPECT_EQ(192.0f, result->distances[1]);
    EXPECT_EQ(432.0f, result->distances[2]);
    DELETE_AND_SET_NULL(result);
    DELETE_AND_SET_NULL(index);
    remove(indexFile.c_str());
}

TEST_F(FAISSWrapperSuite, testHNSWIndex) {
    string indexFile = "./test_hnsw_index.dat";
    unordered_map<string, string> paramMap;
    paramMap[FAISSConstants::PARAM_EF_SEARCH] = "32";
    paramMap[FAISSConstants::PARAM_EF_CONSTRUCTION] = "32";
    FAISSWrapper::dump("IDMap,HNSW16", indexData, paramMap, indexFile);

    Index* index = FAISSWrapper::open(indexFile);
    EXPECT_TRUE(index != NULL);
    float* vector = new float[indexData.dimension];
    for (uint32_t i = 0; i < indexData.dimension; i++) vector[i] = 12.0f;
    FAISSResult* result = FAISSWrapper::search(index, vector, 3);
    EXPECT_TRUE(result != NULL);
    EXPECT_EQ(1, result->ids[0]);
    EXPECT_EQ(2, result->ids[1]);
    EXPECT_EQ(0, result->ids[2]);
    EXPECT_EQ(12.0f, result->distances[0]);
    EXPECT_EQ(192.0f, result->distances[1]);
    EXPECT_EQ(432.0f, result->distances[2]);
    DELETE_AND_SET_NULL(result);
    DELETE_AND_SET_NULL(index);
    remove(indexFile.c_str());
}

END_NAMESPACE(faiss)
END_NAMESPACE(wrapper)
END_NAMESPACE(knn)
END_NAMESPACE(manong)
END_NAMESPACE(xin)