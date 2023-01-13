#include <string>
#include <stdexcept>
#include "object.h"
#include "knn/common/Macro.h"
#include "knn/jni/common/JNIUtil.h"
#include "knn/wrapper/nmslib/NMSLibWrapper.h"
#include "knn/jni/hnsw/xin_manong_search_knn_index_hnsw_HNSWIndexFactory.h"

using namespace std;
using namespace similarity;
using namespace xin::manong::knn::jni::common;
using namespace xin::manong::knn::wrapper::nmslib;

JNIEXPORT jboolean JNICALL Java_xin_manong_search_knn_index_hnsw_HNSWIndexFactory_build(JNIEnv* env, jobject javaObject,
    jintArray ids, jobjectArray data, jstring spaceType, jobjectArray params, jstring path) {
    ObjectVector dataset;
    try {
        int length = JNIUtil::getJavaIntArrayLength(env, ids);
        if (length != JNIUtil::getJavaObjectArrayLength(env, data)) {
            throw std::runtime_error("the length of ids and data are not consistent");
        }
        int* objectIDs = env->GetIntArrayElements(ids, NULL);
        for (int i = 0; i < length; i++) {
            jfloatArray javaVector = (jfloatArray) env->GetObjectArrayElement(data, i);
            int dimension = JNIUtil::getJavaFloatArrayLength(env, javaVector);
            float* vector = env->GetFloatArrayElements(javaVector, NULL);
            dataset.push_back(new Object(objectIDs[i], -1, dimension * sizeof(float), vector));
            env->ReleaseFloatArrayElements(javaVector, vector, JNI_ABORT);
        }
        env->ReleaseIntArrayElements(ids, objectIDs, JNI_ABORT);
        string cppSpaceType = JNIUtil::convertJavaStringToCppString(env, spaceType);
        string cppPath = JNIUtil::convertJavaStringToCppString(env, path);
        vector<string> cppParams = JNIUtil::convertJavaStringArrayToCppStringArray(env, params);
        NMSLibWrapper::dump(cppSpaceType, cppParams, dataset, cppPath);
        for (auto it = dataset.begin(); it != dataset.end(); it++) delete *it;
    } catch (exception& e) {
        for (auto it = dataset.begin(); it != dataset.end(); it++) delete *it;
        JNIUtil::catchCppAndThrowJavaException(env, e);
    }
}