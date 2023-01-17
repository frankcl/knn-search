#ifndef XIN_MANONG_KNN_WRAPPER_FAISS_FAISSDATA_H
#define XIN_MANONG_KNN_WRAPPER_FAISS_FAISSDATA_H

#include "knn/common/Macro.h"

BEGIN_NAMESPACE(xin)
BEGIN_NAMESPACE(manong)
BEGIN_NAMESPACE(knn)
BEGIN_NAMESPACE(wrapper)
BEGIN_NAMESPACE(faiss)

class FAISSData {
public:
    int64_t* ids;
    float* data;
    uint32_t dimension;
    uint32_t size;

public:
    FAISSData();
    virtual ~FAISSData();
    void check() const;

private:
    FAISSData(const FAISSData&);
    FAISSData& operator = (const FAISSData&);
};

END_NAMESPACE(faiss)
END_NAMESPACE(wrapper)
END_NAMESPACE(knn)
END_NAMESPACE(manong)
END_NAMESPACE(xin)

#endif