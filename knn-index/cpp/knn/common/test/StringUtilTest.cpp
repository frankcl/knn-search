#include <string>
#include <gtest/gtest.h>
#include "knn/common/StringUtil.h"

using namespace std;

BEGIN_NAMESPACE(xin)
BEGIN_NAMESPACE(manong)
BEGIN_NAMESPACE(knn)
BEGIN_NAMESPACE(common)

TEST(StringUtilSuite, testTrim) {
    string str(" \r\t\n  abc   \t");
    StringUtil::trim(str);
    EXPECT_EQ("abc", str);
}

TEST(StringUtilSuite, testSplit) {
    string str(",123,456,,789,");
    vector<string> splitList = StringUtil::split(str, ",");
    EXPECT_EQ(6, splitList.size());
    EXPECT_EQ("", splitList[0]);
    EXPECT_EQ("123", splitList[1]);
    EXPECT_EQ("456", splitList[2]);
    EXPECT_EQ("", splitList[3]);    
    EXPECT_EQ("789", splitList[4]);
    EXPECT_EQ("", splitList[5]);    
}

TEST(StringUtilSuite, testInt64ToStr) {
    {
        int64_t v = 345L;
        string str = StringUtil::int64ToStr(v);
        EXPECT_EQ("345", str);
    }
}

TEST(StringUtilSuite, testInt32ToStr) {
    {
        int32_t v = -345;
        string str = StringUtil::int32ToStr(v);
        EXPECT_EQ("-345", str);
    }
}

TEST(StringUtilSuite, testUInt32ToStr) {
    {
        uint32_t v = 345;
        string str = StringUtil::uint32ToStr(v);
        EXPECT_EQ("345", str);
    }
}

TEST(StringUtilSuite, testStrToInt64) {
    {
        string str("123");
        int64_t v = 0L;
        EXPECT_TRUE(StringUtil::strToInt64(str, v));
        EXPECT_EQ(123L, v);
    }
    {
        string str("a123");
        int64_t v = 0L;
        EXPECT_FALSE(StringUtil::strToInt64(str, v));
        EXPECT_EQ(0L, v);
    }    
}

TEST(StringUtilSuite, testStrToInt32) {
    {
        string str("-123");
        int32_t v = 0;
        EXPECT_TRUE(StringUtil::strToInt32(str, v));
        EXPECT_EQ(-123, v);
    }
    {
        string str("a123");
        int32_t v = 0;
        EXPECT_FALSE(StringUtil::strToInt32(str, v));
        EXPECT_EQ(0, v);
    }    
}

TEST(StringUtilSuite, testStrToUInt32) {
    {
        string str("123");
        uint32_t v = 0;
        EXPECT_TRUE(StringUtil::strToUInt32(str, v));
        EXPECT_EQ(123, v);
    }
    {
        string str("a123");
        uint32_t v = 0;
        EXPECT_FALSE(StringUtil::strToUInt32(str, v));
        EXPECT_EQ(0, v);
    }    
}

END_NAMESPACE(common)
END_NAMESPACE(knn)
END_NAMESPACE(manong)
END_NAMESPACE(xin)
