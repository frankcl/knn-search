#include <iostream>
#include <string>
#include <vector>
#include <gtest/gtest.h>
#include "knn/wrapper/nmslib/NMSLibConstants.h"
#include "knn/wrapper/nmslib/NMSLibWrapper.h"

using namespace std;
using namespace similarity;

BEGIN_NAMESPACE(xin)
BEGIN_NAMESPACE(manong)
BEGIN_NAMESPACE(knn)
BEGIN_NAMESPACE(wrapper)
BEGIN_NAMESPACE(nmslib)

class NMSLibWrapperSuite : public testing::Test {
public:
    static void SetUpTestCase() {
        NMSLibWrapper::initLibrary();
    }
};

TEST_F(NMSLibWrapperSuite, testReadWrite) {
    int dimension = 3;
    vector<string> params;
    params.push_back("M=16");
    params.push_back("efConstruction=512");
    params.push_back("indexThreadQty=2");
    string path = "./test_index.hnsw";
    ObjectVector dataset;
    {
        float* vector = new float[dimension];
        vector[0] = 1.0f;
        vector[1] = 1.0f;
        vector[2] = 1.0f;
        dataset.push_back(new Object(1, -1, dimension * sizeof(float), vector));
        DELETE_ARRAY_AND_SET_NULL(vector);
    }
    {
        float* vector = new float[dimension];
        vector[0] = 2.0f;
        vector[1] = 2.0f;
        vector[2] = 2.0f;
        dataset.push_back(new Object(2, -1, dimension * sizeof(float), vector));
        DELETE_ARRAY_AND_SET_NULL(vector);
    }
    {
        float* vector = new float[dimension];
        vector[0] = 3.0f;
        vector[1] = 3.0f;
        vector[2] = 3.0f;
        dataset.push_back(new Object(3, -1, dimension * sizeof(float), vector));
        DELETE_ARRAY_AND_SET_NULL(vector);
    }
    NMSLibWrapper::dump(NMSLibConstants::KNN_SPACE_L2, params, dataset, path);
    for (auto it = dataset.begin(); it != dataset.end(); it++) delete *it;

    params.clear();
    params.push_back("efSearch=512");
    NMSLibIndexWrapper* indexWrapper = NMSLibWrapper::open(path, NMSLibConstants::KNN_SPACE_L2, params);
    EXPECT_TRUE(indexWrapper != nullptr);
    {
        float* vector = new float[dimension];
        vector[0] = 2.0f;
        vector[1] = 3.0f;
        vector[2] = 2.0f;
        KNNQueue<float>* knnQueue = NMSLibWrapper::search(indexWrapper, vector, dimension, 1);
        DELETE_ARRAY_AND_SET_NULL(vector);
        EXPECT_TRUE(knnQueue != NULL);
        EXPECT_EQ(1, knnQueue->Size());
        EXPECT_EQ(1, knnQueue->TopDistance());
        EXPECT_EQ(2, knnQueue->Pop()->id());
        DELETE_AND_SET_NULL(knnQueue);
    }
    {
        float* vector = new float[dimension];
        vector[0] = 2.0f;
        vector[1] = 3.0f;
        vector[2] = 3.0f;
        KNNQueue<float>* knnQueue = NMSLibWrapper::search(indexWrapper, vector, dimension, 1);
        DELETE_ARRAY_AND_SET_NULL(vector);
        EXPECT_TRUE(knnQueue != nullptr);
        EXPECT_EQ(1, knnQueue->Size());
        EXPECT_EQ(1, knnQueue->TopDistance());
        EXPECT_EQ(3, knnQueue->Pop()->id());
        DELETE_AND_SET_NULL(knnQueue);
    }
    NMSLibWrapper::close(indexWrapper);
    EXPECT_EQ(0, remove(path.c_str()));
}

END_NAMESPACE(nmslib)
END_NAMESPACE(wrapper)
END_NAMESPACE(knn)
END_NAMESPACE(manong)
END_NAMESPACE(xin)