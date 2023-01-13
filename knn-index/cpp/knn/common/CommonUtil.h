#ifndef XIN_MANONG_KNN_COMMON_COMMONUTIL_H
#define XIN_MANONG_KNN_COMMON_COMMONUTIL_H

#include <cstdlib>
#include <ctime>
#include "knn/common/Macro.h"

BEGIN_NAMESPACE(xin)
BEGIN_NAMESPACE(manong)
BEGIN_NAMESPACE(knn)
BEGIN_NAMESPACE(common)

class CommonUtil {
public:
    static int32_t randInt(const int32_t max);
    static float randFloat(const float max);
};

inline int32_t CommonUtil::randInt(const int32_t max) {
    srand(time(NULL));
    return rand() % max;
}

inline float CommonUtil::randFloat(const float max) {
    srand(time(NULL));
    int32_t randValue = rand();
    return (float) (randValue / (double) RAND_MAX * max);
}

END_NAMESPACE(common)
END_NAMESPACE(knn)
END_NAMESPACE(manong)
END_NAMESPACE(xin)

#endif
