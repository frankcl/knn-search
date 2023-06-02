#include <string>
#include <stdexcept>
#include <sstream>
#include "knnqueue.h"
#include "knn/common/Macro.h"
#include "knn/jni/common/JNIUtil.h"
#include "knn/wrapper/nmslib/NMSLibIndexWrapper.h"
#include "knn/wrapper/nmslib/NMSLibWrapper.h"
#include "knn/jni/hnsw/xin_manong_search_knn_index_hnsw_HNSWIndex.h"

using namespace std;
using namespace similarity;
using namespace xin::manong::knn::jni::common;
using namespace xin::manong::knn::wrapper::nmslib;

JNIEXPORT void JNICALL Java_xin_manong_search_knn_index_hnsw_HNSWIndex_close
    (JNIEnv* env, jobject javaObject, jlong pointer) {
    try {
        NMSLibIndexWrapper* indexWrapper = reinterpret_cast<NMSLibIndexWrapper*>(pointer);
        NMSLibWrapper::close(indexWrapper);
    } catch (exception& e) {
        JNIUtil::catchCppAndThrowJavaException(env, e);
    }
}

JNIEXPORT jlong JNICALL Java_xin_manong_search_knn_index_hnsw_HNSWIndex_open
    (JNIEnv* env, jobject javaObject, jstring path, jint efSearch, jstring spaceType) {
    try {
        string cppPath = JNIUtil::convertJavaStringToCppString(env, path);
        string cppSpaceType = JNIUtil::convertJavaStringToCppString(env, spaceType);
        vector<string> params;
        stringstream ss;
        ss << "efSearch=" << efSearch;
        params.push_back(ss.str());
        return (jlong) NMSLibWrapper::open(cppPath, cppSpaceType, params);
    } catch (exception& e) {
        JNIUtil::catchCppAndThrowJavaException(env, e);
        return (jlong) 0;
    }
}

JNIEXPORT jobjectArray JNICALL Java_xin_manong_search_knn_index_hnsw_HNSWIndex_search
    (JNIEnv* env, jobject javaObject, jlong pointer, jfloatArray javaVector, jint k) {
    try {
        NMSLibIndexWrapper* indexWrapper = reinterpret_cast<NMSLibIndexWrapper*>(pointer);
        if (indexWrapper == nullptr) throw runtime_error("not an object of NMSLibIndexWrapper");
        int size = JNIUtil::getJavaFloatArrayLength(env, javaVector);
        float* vector = env->GetFloatArrayElements(javaVector, NULL);
        unique_ptr<KNNQueue<float>> knnQueue(NMSLibWrapper::search(indexWrapper, vector, size, k));
        env->ReleaseFloatArrayElements(javaVector, vector, JNI_ABORT);
        int resultSize = knnQueue->Size();
        jclass javaClass = JNIUtil::findClass(env, "xin/manong/search/knn/index/KNNResult");
        jmethodID constructor = JNIUtil::findMethod(env, "xin/manong/search/knn/index/KNNResult", "<init>", "(IF)V");
        jobjectArray knnResults = env->NewObjectArray(resultSize, javaClass, NULL);
        for (int i = 0; i < resultSize; i++) {
            float distance = knnQueue->TopDistance();
            long id = knnQueue->Pop()->id();
            env->SetObjectArrayElement(knnResults, i, env->NewObject(javaClass, constructor, id, distance));
            JNIUtil::checkJNIException(env, "setting object array element failed");
        }
        return knnResults;
    } catch (exception& e) {
        JNIUtil::catchCppAndThrowJavaException(env, e);
        return NULL;
    }
}