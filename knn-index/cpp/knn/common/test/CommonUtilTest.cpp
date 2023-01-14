#include <iostream>
#include <gtest/gtest.h>
#include "knn/common/CommonUtil.h"

using namespace std;

BEGIN_NAMESPACE(xin)
BEGIN_NAMESPACE(manong)
BEGIN_NAMESPACE(knn)
BEGIN_NAMESPACE(common)

TEST(CommonUtilSuite, testRandom) {
    int32_t n = CommonUtil::randInt(10);
    cout << "random for 10 is " << n << endl;
    EXPECT_TRUE(n < 10);
    float f = CommonUtil::randFloat(5.0f);
    EXPECT_TRUE(f < 5.0f);
    cout << "random for 5.0 is " << f << endl;    
}

END_NAMESPACE(common)
END_NAMESPACE(knn)
END_NAMESPACE(manong)
END_NAMESPACE(xin)
