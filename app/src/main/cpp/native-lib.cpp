#include <jni.h>
#include <string>

extern "C" {
JNIEXPORT jstring JNICALL
Java_yu_desk_mococomic_utils_MyConstants_baseUrl(JNIEnv *env, jobject object) {
    std::string value = "";
    return env->NewStringUTF(value.c_str());
}

JNIEXPORT jstring JNICALL
Java_yu_desk_mococomic_utils_MyConstants_webUrl(JNIEnv *env, jobject object) {
    std::string value = "";
    return env->NewStringUTF(value.c_str());
}
}