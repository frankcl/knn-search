#include <exception>
#include "knn/jni/common/JNIUtil.h"
#include "knn/wrapper/nmslib/NMSLibWrapper.h"
#include "knn/jni/hnsw/xin_manong_search_knn_index_hnsw_HNSWLoader.h"

using namespace std;
using namespace xin::manong::knn::jni::common;
using namespace xin::manong::knn::wrapper::nmslib;

JNIEXPORT void JNICALL Java_xin_manong_search_knn_index_hnsw_HNSWLoader_initLibrary(JNIEnv* env, jclass javaClass) {
    try {
        NMSLibWrapper::initLibrary();
    } catch (exception& e) {
        JNIUtil::catchCppAndThrowJavaException(env, e);
    }
}