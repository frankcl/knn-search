#ifndef XIN_MANONG_KNN_JNI_COMMON_JNIUTIL_H
#define XIN_MANONG_KNN_JNI_COMMON_JNIUTIL_H

#include <string>
#include <vector>
#include <stdexcept>
#include <jni.h>
#include "knn/common/Macro.h"

BEGIN_NAMESPACE(xin)
BEGIN_NAMESPACE(manong)
BEGIN_NAMESPACE(knn)
BEGIN_NAMESPACE(jni)
BEGIN_NAMESPACE(common)

class JNIUtil {
public:
    static void throwJavaException(JNIEnv* env, const char* className, const char* message = "");
    static void checkJNIException(JNIEnv* env);
    static void checkJNIException(JNIEnv* env, const std::string& message);
    static void catchCppAndThrowJavaException(JNIEnv* env, const std::exception& e);
    static std::string convertJavaStringToCppString(JNIEnv* env, jstring javaString);
    static std::vector<std::string> convertJavaStringArrayToCppStringArray(JNIEnv* env, jobjectArray javaStringArray);
    static int getJavaObjectArrayLength(JNIEnv* env, jobjectArray javaArray);
    static int getJavaFloatArrayLength(JNIEnv* env, jfloatArray javaArray);
    static int getJavaIntArrayLength(JNIEnv* env, jintArray javaArray);
};

END_NAMESPACE(common)
END_NAMESPACE(jni)
END_NAMESPACE(knn)
END_NAMESPACE(manong)
END_NAMESPACE(xin)

#endif

