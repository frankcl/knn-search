#ifndef XIN_MANONG_KNN_WRAPPER_NMSLIB_NMSLIBINDEXWRAPPER_H
#define XIN_MANONG_KNN_WRAPPER_NMSLIB_NMSLIBINDEXWRAPPER_H

#include <memory>
#include "index.h"
#include "object.h"
#include "params.h"
#include "space.h"
#include "methodfactory.h"
#include "spacefactory.h"
#include "knn/common/Macro.h"

BEGIN_NAMESPACE(xin)
BEGIN_NAMESPACE(manong)
BEGIN_NAMESPACE(knn)
BEGIN_NAMESPACE(wrapper)
BEGIN_NAMESPACE(nmslib)

class NMSLibIndexWrapper {
public:
    NMSLibIndexWrapper(const std::string& spaceType);
    ~NMSLibIndexWrapper();

private:
    NMSLibIndexWrapper(const NMSLibIndexWrapper&);
    NMSLibIndexWrapper& operator = (const NMSLibIndexWrapper&);

public:
    std::unique_ptr<similarity::Index<float>> index;
    std::unique_ptr<similarity::Space<float>> space;

private:
    similarity::ObjectVector data;
};

END_NAMESPACE(nmslib)
END_NAMESPACE(wrapper)
END_NAMESPACE(knn)
END_NAMESPACE(manong)
END_NAMESPACE(xin)

#endif