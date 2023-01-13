#include "knn/jni/common/JNIUtil.h"

BEGIN_NAMESPACE(xin)
BEGIN_NAMESPACE(manong)
BEGIN_NAMESPACE(knn)
BEGIN_NAMESPACE(jni)
BEGIN_NAMESPACE(common)

using namespace std;

void JNIUtil::throwJavaException(JNIEnv* env, const char* className, const char* message) {
    jclass javaExceptionClass = env->FindClass(className);
    string cppClassName(className);
    checkJNIException(env, "class[" + cppClassName + "] is not found");
    if (javaExceptionClass != NULL) env->ThrowNew(javaExceptionClass, message);
}

void JNIUtil::checkJNIException(JNIEnv* env) {
    checkJNIException(env, "unknown exception");
}

void JNIUtil::checkJNIException(JNIEnv* env, const string& message) {
    if (env->ExceptionCheck() != JNI_TRUE) return;
    jthrowable exception = env->ExceptionOccurred();
    if (exception == NULL) {
        throwJavaException(env, "java/lang/Exception", message.c_str());
        env->ExceptionClear();
        return;
    }
    env->Throw(exception);
    env->ExceptionClear();
}

void JNIUtil::catchCppAndThrowJavaException(JNIEnv* env, const exception& e) {
    throwJavaException(env, "java/lang/Exception", e.what());
}

string JNIUtil::convertJavaStringToCppString(JNIEnv* env, jstring javaString) {
    const char* charArray = env->GetStringUTFChars(javaString, NULL);
    if (charArray == NULL) {
        checkJNIException(env, "convert java string to cpp string failed");
        throw runtime_error("convert java string to cpp string failed");
    }
    string cppString(charArray);
    env->ReleaseStringUTFChars(javaString, charArray);
    return cppString;
}

vector<string> JNIUtil::convertJavaStringArrayToCppStringArray(JNIEnv* env, jobjectArray javaStringArray) {
    int length = getJavaObjectArrayLength(env, javaStringArray);
    vector<string> cppStringArray;
    for (int i = 0; i < length; i++) {
        jstring javaString = (jstring) (env->GetObjectArrayElement(javaStringArray, i));
        string cppString = convertJavaStringToCppString(env, javaString);
        cppStringArray.push_back(cppString);
    }
    return cppStringArray;
}

int JNIUtil::getJavaObjectArrayLength(JNIEnv* env, jobjectArray javaArray) {
    int length = env->GetArrayLength(javaArray);
    checkJNIException(env, "get object array length failed");
    return length;
}

int JNIUtil::getJavaFloatArrayLength(JNIEnv* env, jfloatArray javaArray) {
    int length = env->GetArrayLength(javaArray);
    checkJNIException(env, "get float array length failed");
    return length;
}

int JNIUtil::getJavaIntArrayLength(JNIEnv* env, jintArray javaArray) {
    int length = env->GetArrayLength(javaArray);
    checkJNIException(env, "get int array length failed");
    return length;
}

END_NAMESPACE(common)
END_NAMESPACE(jni)
END_NAMESPACE(knn)
END_NAMESPACE(manong)
END_NAMESPACE(xin)
