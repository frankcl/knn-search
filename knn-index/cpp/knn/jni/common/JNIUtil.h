#ifndef XIN_MANONG_KNN_JNI_COMMON_JNIUTIL_H
#define XIN_MANONG_KNN_JNI_COMMON_JNIUTIL_H

#include <string>
#include <vector>
#include <unordered_map>
#include <jni.h>
#include "knn/common/Macro.h"

BEGIN_NAMESPACE(xin)
BEGIN_NAMESPACE(manong)
BEGIN_NAMESPACE(knn)
BEGIN_NAMESPACE(jni)
BEGIN_NAMESPACE(common)

class JNIUtil {
private:
    static std::unordered_map<std::string, jclass> cachedClasses;
    static std::unordered_map<std::string, jmethodID> cachedMethods;

public:
    static void throwJavaException(JNIEnv* env, const char* className, const char* message = "");
    static void checkJNIException(JNIEnv* env);
    static void checkJNIException(JNIEnv* env, const std::string& message);
    static void catchCppAndThrowJavaException(JNIEnv* env, const std::exception& e);
    static std::string convertJavaStringToCppString(JNIEnv* env, jstring javaString);
    static std::vector<std::string> convertJavaStringArrayToCppStringArray(JNIEnv* env, jobjectArray javaStringArray);
    static int64_t* convertJavaIntArrayToCppInt64Array(JNIEnv* env, jintArray javaIntArray);
    static float* convertJava2DVectorToCppVector(JNIEnv* env, jobjectArray java2DVector);
    static std::unordered_map<std::string, std::string> convertJavaMapToCppMap(JNIEnv* env, jobject javaMap);
    static int get2DVectorDimension(JNIEnv* env, jobjectArray java2DVector);
    static int getJavaObjectArrayLength(JNIEnv* env, jobjectArray javaArray);
    static int getJavaFloatArrayLength(JNIEnv* env, jfloatArray javaArray);
    static int getJavaIntArrayLength(JNIEnv* env, jintArray javaArray);
    static jclass findClass(JNIEnv* env, const std::string& className);
    static jmethodID findMethod(JNIEnv* env, const std::string& className,
                                const std::string& methodName, const std::string& methodSign);
};

END_NAMESPACE(common)
END_NAMESPACE(jni)
END_NAMESPACE(knn)
END_NAMESPACE(manong)
END_NAMESPACE(xin)

#endif

