#include <stdexcept>
#include <sstream>
#include "knn/wrapper/faiss/FAISSData.h"

BEGIN_NAMESPACE(xin)
BEGIN_NAMESPACE(manong)
BEGIN_NAMESPACE(knn)
BEGIN_NAMESPACE(wrapper)
BEGIN_NAMESPACE(faiss)

using namespace std;

FAISSData::FAISSData() {
}

FAISSData::~FAISSData() {
    DELETE_ARRAY_AND_SET_NULL(ids);
    DELETE_ARRAY_AND_SET_NULL(data);
}

void FAISSData::check() const {
    if (ids == NULL) throw runtime_error("data id list is empty");
    if (data == NULL) throw runtime_error("data is empty");
    if (dimension <= 0) {
        stringstream ss;
        ss << "invalid vector dimension[" << dimension << "]";
        throw runtime_error(ss.str());
    }
    if (size <= 0) {
        stringstream ss;
        ss << "invalid data size[" << size << "]";
        throw runtime_error(ss.str());
    }
}


END_NAMESPACE(faiss)
END_NAMESPACE(wrapper)
END_NAMESPACE(knn)
END_NAMESPACE(manong)
END_NAMESPACE(xin)
