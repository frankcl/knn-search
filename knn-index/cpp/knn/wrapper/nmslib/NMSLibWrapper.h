#ifndef XIN_MANONG_KNN_WRAPPER_NMSLIB_NMSLIBWRAPPER_H
#define XIN_MANONG_KNN_WRAPPER_NMSLIB_NMSLIBWRAPPER_H

#include "object.h"
#include "knnqueue.h"
#include "knn/common/Macro.h"
#include "knn/wrapper/nmslib/NMSLibIndexWrapper.h"

BEGIN_NAMESPACE(xin)
BEGIN_NAMESPACE(manong)
BEGIN_NAMESPACE(knn)
BEGIN_NAMESPACE(wrapper)
BEGIN_NAMESPACE(nmslib)

class NMSLibWrapper {
private:
    NMSLibWrapper(const NMSLibIndexWrapper&);
    NMSLibWrapper& operator = (const NMSLibIndexWrapper&);

public:
    static void initLibrary();
    static void dump(const std::string& spaceType, const std::vector<std::string>& params,
                     const similarity::ObjectVector& dataset, const std::string& path);
    static NMSLibIndexWrapper* open(const std::string& path, const std::string& spaceType,
                                    const std::vector<std::string>& params);
    static void close(const NMSLibIndexWrapper* indexWrapper);
    static similarity::KNNQueue<float>* search(const NMSLibIndexWrapper* indexWrapper,
                                               const float* vector, const int size, const int k);
};

END_NAMESPACE(nmslib)
END_NAMESPACE(wrapper)
END_NAMESPACE(knn)
END_NAMESPACE(manong)
END_NAMESPACE(xin)

#endif