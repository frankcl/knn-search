#ifndef XIN_MANONG_KNN_WRAPPER_FAISS_FAISSRESULT_H
#define XIN_MANONG_KNN_WRAPPER_FAISS_FAISSRESULT_H

#include <cstdint>
#include "knn/common/Macro.h"

BEGIN_NAMESPACE(xin)
BEGIN_NAMESPACE(manong)
BEGIN_NAMESPACE(knn)
BEGIN_NAMESPACE(wrapper)
BEGIN_NAMESPACE(faiss)

class FAISSResult {
public:
    uint32_t n;
    int64_t* ids;
    float* distances;

public:
    FAISSResult(const uint32_t n);
    virtual ~FAISSResult();

private:
    FAISSResult(const FAISSResult&);
    FAISSResult& operator = (const FAISSResult&);
};

END_NAMESPACE(faiss)
END_NAMESPACE(wrapper)
END_NAMESPACE(knn)
END_NAMESPACE(manong)
END_NAMESPACE(xin)

#endif