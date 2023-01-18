#include <stdexcept>
#include "knn/jni/common/JNIUtil.h"

using namespace std;

BEGIN_NAMESPACE(xin)
BEGIN_NAMESPACE(manong)
BEGIN_NAMESPACE(knn)
BEGIN_NAMESPACE(jni)
BEGIN_NAMESPACE(common)

unordered_map<string, jclass> JNIUtil::cachedClasses;
unordered_map<string, jmethodID> JNIUtil::cachedMethods;

void JNIUtil::throwJavaException(JNIEnv* env, const char* className, const char* message) {
    jclass javaExceptionClass = env->FindClass(className);
    string cppClassName(className);
    checkJNIException(env, "class[" + cppClassName + "] is not found");
    if (javaExceptionClass != NULL) {
        env->ThrowNew(javaExceptionClass, message);
        env->DeleteLocalRef(javaExceptionClass);
    }
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
    if (charArray == nullptr) {
        checkJNIException(env, "convert java string to cpp string failed");
        throw runtime_error("convert java string to cpp string failed");
    }
    string cppString(charArray);
    env->ReleaseStringUTFChars(javaString, charArray);
    return cppString;
}

jclass JNIUtil::findClass(JNIEnv* env, const string& className) {
    if (cachedClasses.find(className) != cachedClasses.end()) return cachedClasses[className];
    jclass tempLocalClassRef = env->FindClass(className.c_str());
    cachedClasses[className] = (jclass) env->NewGlobalRef(tempLocalClassRef);
    env->DeleteLocalRef(tempLocalClassRef);
    return cachedClasses[className];
}

jmethodID JNIUtil::findMethod(JNIEnv* env, const string& className, const string& methodName,
                              const string& methodSign) {
    string key = className + "_" + methodName + "_" + methodSign;
    if (cachedMethods.find(key) != cachedMethods.end()) return cachedMethods[key];
    jclass javaClass = findClass(env, className);
    cachedMethods[key] = env->GetMethodID(javaClass, methodName.c_str(), methodSign.c_str());
    return cachedMethods[key];
}

unordered_map<string, string> JNIUtil::convertJavaMapToCppMap(JNIEnv* env, jobject javaMap) {
    unordered_map<string, string> cppMap;
    jmethodID javaEntrySetMethod = findMethod(env, "java/util/Map", "entrySet", "()Ljava/util/Set;");
    jobject javaEntrySet = env->CallObjectMethod(javaMap, javaEntrySetMethod);
    checkJNIException(env, "call entrySet method failed for class java/util/Map");
    jmethodID javaIteratorMethod = findMethod(env, "java/util/Set", "iterator", "()Ljava/util/Iterator;");
    jobject javaIterator = env->CallObjectMethod(javaEntrySet, javaIteratorMethod);
    checkJNIException(env, "call iterator method failed for class java/util/Set");

    jmethodID javaHasNextMethod = findMethod(env, "java/util/Iterator", "hasNext", "()Z");
    jmethodID javaNextMethod = findMethod(env, "java/util/Iterator", "next", "()Ljava/lang/Object;");
    jmethodID javaGetKeyMethod = findMethod(env, "java/util/Map$Entry", "getKey", "()Ljava/lang/Object;");
    jmethodID javaGetValueMethod = findMethod(env, "java/util/Map$Entry", "getValue", "()Ljava/lang/Object;");

    while (env->CallBooleanMethod(javaIterator, javaHasNextMethod)) {
        checkJNIException(env, "call hasNext method failed for class java/util/Iterator");
        jobject javaEntry = env->CallObjectMethod(javaIterator, javaNextMethod);
        checkJNIException(env, "call next method failed for class java/util/Iterator");
        jstring javaKey = (jstring) env->CallObjectMethod(javaEntry, javaGetKeyMethod);
        checkJNIException(env, "call getKey method failed for class java/util/Map$Entry");
        jstring javaValue = (jstring) env->CallObjectMethod(javaEntry, javaGetValueMethod);
        checkJNIException(env, "call getValue method failed for class java/util/Map$Entry");
        string cppKey = convertJavaStringToCppString(env, javaKey);
        string cppValue = convertJavaStringToCppString(env, javaValue);
        cppMap[cppKey] = cppValue;
        env->DeleteLocalRef(javaValue);
        env->DeleteLocalRef(javaKey);
        env->DeleteLocalRef(javaEntry);
    }
    checkJNIException(env, "call hasNext method failed for class java/util/Iterator");
    return cppMap;
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

int64_t* JNIUtil::convertJavaIntArrayToCppInt64Array(JNIEnv* env, jintArray javaIntArray) {
    int length = getJavaIntArrayLength(env, javaIntArray);
    jint* cppIntArray = env->GetIntArrayElements(javaIntArray, NULL);
    int64_t* cppInt64Array = new int64_t[length];
    for (int i = 0; i < length; i++) cppInt64Array[i] = (int64_t) cppIntArray[i];
    env->ReleaseIntArrayElements(javaIntArray, cppIntArray, JNI_ABORT);
    return cppInt64Array;
}

float* JNIUtil::convertJava2DVectorToCppVector(JNIEnv* env, jobjectArray java2DVector) {
    int length = getJavaObjectArrayLength(env, java2DVector);
    float* vector = nullptr;
    for (int i = 0; i < length; i++) {
        jfloatArray javaVector = (jfloatArray) env->GetObjectArrayElement(java2DVector, i);
        float* cppVector = env->GetFloatArrayElements(javaVector, NULL);
        int dimension = getJavaFloatArrayLength(env, javaVector);
        if (vector == nullptr) vector = new float[length * dimension];
        for (int j = 0; j < dimension; j++) vector[i * dimension + j] = cppVector[j];
        env->ReleaseFloatArrayElements(javaVector, cppVector, JNI_ABORT);
    }
    return vector;
}

int JNIUtil::get2DVectorDimension(JNIEnv* env, jobjectArray java2DVector) {
    int length = getJavaObjectArrayLength(env, java2DVector);
    if (length == 0) throw runtime_error("java 2D vector length is 0");
    jfloatArray javaVector = (jfloatArray) env->GetObjectArrayElement(java2DVector, 0);
    return getJavaFloatArrayLength(env, javaVector);
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
