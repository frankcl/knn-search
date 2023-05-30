#ifndef XIN_MANONG_KNN_WRAPPER_FAISS_FAISSCONSTANTS_H
#define XIN_MANONG_KNN_WRAPPER_FAISS_FAISSCONSTANTS_H

#include <string>
#include "knn/common/Macro.h"

BEGIN_NAMESPACE(xin)
BEGIN_NAMESPACE(manong)
BEGIN_NAMESPACE(knn)
BEGIN_NAMESPACE(wrapper)
BEGIN_NAMESPACE(faiss)

class FAISSConstants {
private:
    FAISSConstants(const FAISSConstants&);
    FAISSConstants& operator = (const FAISSConstants&);

public:
    const static std::string PARAM_EF_CONSTRUCTION;
    const static std::string PARAM_EF_SEARCH;
    const static std::string PARAM_N_PROBE;
    const static std::string PARAM_INDEX_THREAD_QUANTITY;
};

END_NAMESPACE(faiss)
END_NAMESPACE(wrapper)
END_NAMESPACE(knn)
END_NAMESPACE(manong)
END_NAMESPACE(xin)

#endif