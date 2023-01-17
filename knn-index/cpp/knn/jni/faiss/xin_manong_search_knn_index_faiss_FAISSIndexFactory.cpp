#include <string>
#include <stdexcept>
#include "knn/common/Macro.h"
#include "knn/jni/common/JNIUtil.h"
#include "knn/wrapper/faiss/FAISSData.h"
#include "knn/wrapper/faiss/FAISSWrapper.h"
#include "knn/jni/faiss/xin_manong_search_knn_index_faiss_FAISSIndexFactory.h"

using namespace std;
using namespace xin::manong::knn::jni::common;
using namespace xin::manong::knn::wrapper::faiss;

JNIEXPORT jboolean JNICALL Java_xin_manong_search_knn_index_faiss_FAISSIndexFactory_build
  (JNIEnv* env, jobject javaObject, jintArray ids, jobjectArray data,
   jstring description, jstring path, jobject paramMap) {
    try {
        int length = JNIUtil::getJavaIntArrayLength(env, ids);
        if (length != JNIUtil::getJavaObjectArrayLength(env, data)) {
            throw std::runtime_error("the length of ids and data are not consistent");
        }
        FAISSData indexData;
        indexData.size = length;
        indexData.dimension = JNIUtil::get2DVectorDimension(env, data);
        indexData.ids = JNIUtil::convertJavaIntArrayToCppInt64Array(env, ids);
        indexData.data = JNIUtil::convertJava2DVectorToCppVector(env, data);
        string cppDescription = JNIUtil::convertJavaStringToCppString(env, description);
        string cppPath = JNIUtil::convertJavaStringToCppString(env, path);
        unordered_map<string, int32_t> cppParamMap = JNIUtil::convertJavaMapToCppInt32Map(env, paramMap);
        FAISSWrapper::dump(cppDescription, indexData, cppParamMap, cppPath);
        return true;
    } catch (exception& e) {
        JNIUtil::catchCppAndThrowJavaException(env, e);
        return false;
    }
}