/**
 * File generated by Jenny -- https://github.com/LanderlYoung/Jenny
 *
 * DO NOT EDIT THIS FILE.
 *
 * For bug report, please refer to github issue tracker https://github.com/LanderlYoung/Jenny/issues,
 * or contact author landerlyoung@gmail.com.
 */

/* C++ header file for class io/github/landerlyoung/jennysample/ComputeInNative */
#pragma once

#include <jni.h>


namespace ComputeInNative {

// DO NOT modify
static constexpr auto FULL_CLASS_NAME = "io/github/landerlyoung/jennysample/ComputeInNative";


/*
 * Class:     io_github_landerlyoung_jennysample_ComputeInNative
 * Method:    public boolean init()
 * Signature: ()Z
 */
jboolean JNICALL init(JNIEnv *env, jobject thiz);

/*
 * Class:     io_github_landerlyoung_jennysample_ComputeInNative
 * Method:    public void release()
 * Signature: ()V
 */
void JNICALL release(JNIEnv *env, jobject thiz);

/*
 * Class:     io_github_landerlyoung_jennysample_ComputeInNative
 * Method:    public void setParam(java.util.Map<java.lang.String,java.lang.String> globalHttpParam)
 * Signature: (Ljava/util/Map;)V
 */
void JNICALL setParam(JNIEnv *env, jobject thiz, jobject globalHttpParam);

/*
 * Class:     io_github_landerlyoung_jennysample_ComputeInNative
 * Method:    public java.util.Map<java.lang.String,java.lang.String> getGlobalParam()
 * Signature: ()Ljava/util/Map;
 */
jobject JNICALL getGlobalParam(JNIEnv *env, jobject thiz);

/*
 * Class:     io_github_landerlyoung_jennysample_ComputeInNative
 * Method:    public boolean request(java.lang.String json, io.github.landerlyoung.jennysample.RequestListener listener)
 * Signature: (Ljava/lang/String;Lio/github/landerlyoung/jennysample/RequestListener;)Z
 */
jboolean JNICALL request(JNIEnv *env, jobject thiz, jstring json, jobject listener);

/**
* register Native functions
* @returns success or not
*/
inline bool registerNativeFunctions(JNIEnv *env) {
   const JNINativeMethod gsNativeMethods[] = {
       {
           /* method name      */ const_cast<char *>("init"),
           /* method signature */ const_cast<char *>("()Z"),
           /* function pointer */ reinterpret_cast<void *>(init)
       },
       {
           /* method name      */ const_cast<char *>("release"),
           /* method signature */ const_cast<char *>("()V"),
           /* function pointer */ reinterpret_cast<void *>(release)
       },
       {
           /* method name      */ const_cast<char *>("setParam"),
           /* method signature */ const_cast<char *>("(Ljava/util/Map;)V"),
           /* function pointer */ reinterpret_cast<void *>(setParam)
       },
       {
           /* method name      */ const_cast<char *>("getGlobalParam"),
           /* method signature */ const_cast<char *>("()Ljava/util/Map;"),
           /* function pointer */ reinterpret_cast<void *>(getGlobalParam)
       },
       {
           /* method name      */ const_cast<char *>("request"),
           /* method signature */ const_cast<char *>("(Ljava/lang/String;Lio/github/landerlyoung/jennysample/RequestListener;)Z"),
           /* function pointer */ reinterpret_cast<void *>(request)
       }
   };
   const int gsMethodCount =
       sizeof(gsNativeMethods) / sizeof(JNINativeMethod);

   bool success = false;
   jclass clazz = env->FindClass(FULL_CLASS_NAME);
   if (clazz != nullptr) {
       success = 0 == env->RegisterNatives(clazz, gsNativeMethods, gsMethodCount);
       env->DeleteLocalRef(clazz);
   }
   return success;
}

} // endof namespace ComputeInNative


