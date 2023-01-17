#include "knn/wrapper/nmslib/NMSLibConstants.h"
#include "knn/wrapper/nmslib/NMSLibIndexWrapper.h"

using namespace std;
using namespace similarity;

BEGIN_NAMESPACE(xin)
BEGIN_NAMESPACE(manong)
BEGIN_NAMESPACE(knn)
BEGIN_NAMESPACE(wrapper)
BEGIN_NAMESPACE(nmslib)

NMSLibIndexWrapper::NMSLibIndexWrapper(const string& spaceType) {
    space.reset(SpaceFactoryRegistry<float>::Instance().CreateSpace(spaceType, AnyParams()));
    index.reset(MethodFactoryRegistry<float>::Instance().CreateMethod(false,
            NMSLibConstants::KNN_ALGO_HNSW, spaceType, *space, data));
}

NMSLibIndexWrapper::~NMSLibIndexWrapper() {
}

END_NAMESPACE(nmslib)
END_NAMESPACE(wrapper)
END_NAMESPACE(knn)
END_NAMESPACE(manong)
END_NAMESPACE(xin)
