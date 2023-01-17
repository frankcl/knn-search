#include <string>
#include <stdexcept>
#include "knn/common/Macro.h"
#include "knn/jni/common/JNIUtil.h"
#include "knn/wrapper/faiss/FAISSWrapper.h"
#include "knn/jni/faiss/xin_manong_search_knn_index_faiss_FAISSIndex.h"

using namespace std;
using namespace faiss;
using namespace xin::manong::knn::jni::common;
using namespace xin::manong::knn::wrapper::faiss;

JNIEXPORT void JNICALL Java_xin_manong_search_knn_index_faiss_FAISSIndex_close
  (JNIEnv* env, jobject javaObject, jlong pointer) {
    try {
        Index* index = reinterpret_cast<Index*>(pointer);
        FAISSWrapper::close(index);
    } catch (exception& e) {
        JNIUtil::catchCppAndThrowJavaException(env, e);
    }
}

JNIEXPORT jlong JNICALL Java_xin_manong_search_knn_index_faiss_FAISSIndex_open
  (JNIEnv* env, jobject javaObject, jstring path) {
    try {
        string cppPath = JNIUtil::convertJavaStringToCppString(env, path);
        return (jlong) FAISSWrapper::open(cppPath);
    } catch (exception& e) {
        JNIUtil::catchCppAndThrowJavaException(env, e);
        return (jlong) NULL;
    }
}

JNIEXPORT jobjectArray JNICALL Java_xin_manong_search_knn_index_faiss_FAISSIndex_search
  (JNIEnv* env, jobject javaObject, jlong pointer, jfloatArray javaVector, jint k) {
    try {
        Index* index = reinterpret_cast<Index*>(pointer);
        if (index == nullptr) throw runtime_error("not an object of faiss::Index");
        float* vector = env->GetFloatArrayElements(javaVector, NULL);
        unique_ptr<FAISSResult> result(FAISSWrapper::search(index, vector, k));
        env->ReleaseFloatArrayElements(javaVector, vector, JNI_ABORT);
        jclass javaClass = env->FindClass("xin/manong/search/knn/index/KNNResult");
        jmethodID constructor = env->GetMethodID(javaClass, "<init>", "(IF)V");
        jobjectArray knnResults = env->NewObjectArray(result->n, javaClass, NULL);
        for (size_t i = 0; i < result->n; i++) {
            float distance = result->distances[i];
            long id = result->ids[i];
            env->SetObjectArrayElement(knnResults, i, env->NewObject(javaClass, constructor, id, distance));
            JNIUtil::checkJNIException(env, "setting object array element failed");
        }
        env->DeleteLocalRef(javaClass);
        return knnResults;
    } catch (exception& e) {
        JNIUtil::catchCppAndThrowJavaException(env, e);
        return NULL;
    }
}