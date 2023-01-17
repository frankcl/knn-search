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
};

TEST_F(FAISSWrapperSuite, testReadWrite) {

}

END_NAMESPACE(faiss)
END_NAMESPACE(wrapper)
END_NAMESPACE(knn)
END_NAMESPACE(manong)
END_NAMESPACE(xin)