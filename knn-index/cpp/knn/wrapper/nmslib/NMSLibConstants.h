#ifndef XIN_MANONG_KNN_WRAPPER_NMSLIB_NMSLIBCONSTANTS_H
#define XIN_MANONG_KNN_WRAPPER_NMSLIB_NMSLIBCONSTANTS_H

#include <string>
#include "knn/common/Macro.h"

BEGIN_NAMESPACE(xin)
BEGIN_NAMESPACE(manong)
BEGIN_NAMESPACE(knn)
BEGIN_NAMESPACE(wrapper)
BEGIN_NAMESPACE(nmslib)

class NMSLibConstants {
private:
    NMSLibConstants(const NMSLibConstants&);
    NMSLibConstants& operator = (const NMSLibConstants&);

public:
    const static std::string KNN_ALGO_HNSW;
    const static std::string KNN_SPACE_L2;
    const static std::string KNN_SPACE_COSINE;
};

END_NAMESPACE(nmslib)
END_NAMESPACE(wrapper)
END_NAMESPACE(knn)
END_NAMESPACE(manong)
END_NAMESPACE(xin)

#endif