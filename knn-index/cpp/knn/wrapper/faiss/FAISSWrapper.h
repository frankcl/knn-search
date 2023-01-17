#ifndef XIN_MANONG_KNN_WRAPPER_FAISS_FAISSWRAPPER_H
#define XIN_MANONG_KNN_WRAPPER_FAISS_FAISSWRAPPER_H

#include <string>
#include <unordered_map>
#include <faiss/Index.h>
#include "knn/common/Macro.h"
#include "knn/wrapper/faiss/FAISSData.h"
#include "knn/wrapper/faiss/FAISSResult.h"

BEGIN_NAMESPACE(xin)
BEGIN_NAMESPACE(manong)
BEGIN_NAMESPACE(knn)
BEGIN_NAMESPACE(wrapper)
BEGIN_NAMESPACE(faiss)

class FAISSWrapper {
private:
    FAISSWrapper(const FAISSWrapper&);
    FAISSWrapper& operator = (const FAISSWrapper&);

private:
    static ::faiss::Index* getInnerIndexImpl(::faiss::Index* index);
    static void setIndexParameters(::faiss::Index* index,
                                   const std::unordered_map<std::string, int32_t>& paramMap);

public:
    static ::faiss::Index* open(const std::string& path);
    static void close(::faiss::Index* index);
    static FAISSResult* search(const ::faiss::Index* index, const float* vector, const uint32_t k);
    static void dump(const std::string& indexDescription, const FAISSData& indexData,
                     const std::unordered_map<std::string, int32_t>& paramMap, const std::string& path);
};

END_NAMESPACE(faiss)
END_NAMESPACE(wrapper)
END_NAMESPACE(knn)
END_NAMESPACE(manong)
END_NAMESPACE(xin)

#endif