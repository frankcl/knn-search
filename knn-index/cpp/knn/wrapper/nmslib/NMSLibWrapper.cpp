#include <memory>
#include "init.h"
#include "index.h"
#include "object.h"
#include "knnquery.h"
#include "params.h"
#include "space.h"
#include "methodfactory.h"
#include "spacefactory.h"
#include "knn/wrapper/nmslib/NMSLibConstants.h"
#include "knn/wrapper/nmslib/NMSLibWrapper.h"

BEGIN_NAMESPACE(xin)
BEGIN_NAMESPACE(manong)
BEGIN_NAMESPACE(knn)
BEGIN_NAMESPACE(wrapper)
BEGIN_NAMESPACE(nmslib)

using namespace std;
using namespace similarity;

void NMSLibWrapper::initLibrary() {
    initLibrary();
}

void NMSLibWrapper::dump(const string& spaceType, const vector<std::string>& params,
                         const ObjectVector& dataset, const string& path) {
    unique_ptr<Space<float>> space(SpaceFactoryRegistry<float>::Instance().CreateSpace(spaceType, AnyParams()));
    unique_ptr<Index<float>> index(MethodFactoryRegistry<float>::Instance().CreateMethod(
            false, NMSLibConstants::KNN_ALGO_HNSW, spaceType, *space, dataset));
    if (!params.empty()) index->CreateIndex(AnyParams(params));
    index->SaveIndex(path);
}

NMSLibIndexWrapper* NMSLibWrapper::open(const string& path, const string& spaceType, const vector<string>& params) {
    NMSLibIndexWrapper *indexWrapper = new NMSLibIndexWrapper(spaceType);
    indexWrapper->index->LoadIndex(path);
    if (!params.empty()) indexWrapper->index->SetQueryTimeParams(AnyParams(params));
    return indexWrapper;
}

void NMSLibWrapper::close(const NMSLibIndexWrapper* indexWrapper) {
    delete indexWrapper;
}

similarity::KNNQueue<float>* NMSLibWrapper::search(const NMSLibIndexWrapper* indexWrapper,
                                                   const float* vector, const int size, const int k) {
    unique_ptr<const Object> queryObject(new Object(-1, -1, size, vector));
    KNNQuery<float> knnQuery(*(indexWrapper->space), queryObject.get(), k);
    indexWrapper->index->Search(&knnQuery);
    return knnQuery.Result()->Clone();
}

END_NAMESPACE(nmslib)
END_NAMESPACE(wrapper)
END_NAMESPACE(knn)
END_NAMESPACE(manong)
END_NAMESPACE(xin)
