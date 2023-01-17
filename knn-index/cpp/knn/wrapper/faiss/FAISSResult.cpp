#include "knn/wrapper/faiss/FAISSResult.h"

BEGIN_NAMESPACE(xin)
BEGIN_NAMESPACE(manong)
BEGIN_NAMESPACE(knn)
BEGIN_NAMESPACE(wrapper)
BEGIN_NAMESPACE(faiss)

using namespace std;

FAISSResult::FAISSResult(const uint32_t n) {
    this->n = 0;
    ids = new int64_t[n];
    distances = new float[n];
}

FAISSResult::~FAISSResult() {
    DELETE_ARRAY_AND_SET_NULL(ids);
    DELETE_ARRAY_AND_SET_NULL(distances);
}

END_NAMESPACE(faiss)
END_NAMESPACE(wrapper)
END_NAMESPACE(knn)
END_NAMESPACE(manong)
END_NAMESPACE(xin)
