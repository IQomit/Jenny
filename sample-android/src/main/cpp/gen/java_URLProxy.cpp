/**
 * File generated by Jenny -- https://github.com/LanderlYoung/Jenny
 *
 * DO NOT EDIT THIS FILE.
 *
 * For bug report, please refer to github issue tracker https://github.com/LanderlYoung/Jenny/issues,
 * or contact author landerlyoung@gmail.com.
 */
#include "java_URLProxy.h"


// external logger function passed by jenny.errorLoggerFunction
void jennySampleErrorLog(JNIEnv* env, const char* error);


namespace java {

jclass URLProxy::sClazz = nullptr;

// thread safe init
std::mutex URLProxy::sInitLock;
std::atomic_bool URLProxy::sInited;

/*static*/ bool URLProxy::initClazz(JNIEnv *env) {
#define JENNY_CHECK_NULL(val)                      \
       do {                                        \
           if ((val) == nullptr) {                 \
               jennySampleErrorLog(env, "can't init URLProxy::" #val); \
               return false;                       \
           }                                       \
       } while(false)

    if (!sInited) {
        std::lock_guard<std::mutex> lg(sInitLock);
        if (!sInited) {
            auto clazz = env->FindClass(FULL_CLASS_NAME);
            JENNY_CHECK_NULL(clazz);
            sClazz = reinterpret_cast<jclass>(env->NewGlobalRef(clazz));
            env->DeleteLocalRef(clazz);
            JENNY_CHECK_NULL(sClazz);

            sConstruct_0 = env->GetMethodID(sClazz, "<init>", "(Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;)V");
            JENNY_CHECK_NULL(sConstruct_0);

            sConstruct_1 = env->GetMethodID(sClazz, "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
            JENNY_CHECK_NULL(sConstruct_1);

            sConstruct_2 = env->GetMethodID(sClazz, "<init>", "(Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;Ljava/net/URLStreamHandler;)V");
            JENNY_CHECK_NULL(sConstruct_2);

            sConstruct_3 = env->GetMethodID(sClazz, "<init>", "(Ljava/lang/String;)V");
            JENNY_CHECK_NULL(sConstruct_3);

            sConstruct_4 = env->GetMethodID(sClazz, "<init>", "(Ljava/net/URL;Ljava/lang/String;)V");
            JENNY_CHECK_NULL(sConstruct_4);

            sConstruct_5 = env->GetMethodID(sClazz, "<init>", "(Ljava/net/URL;Ljava/lang/String;Ljava/net/URLStreamHandler;)V");
            JENNY_CHECK_NULL(sConstruct_5);


            sMethod_getQuery_0 = env->GetMethodID(sClazz, "getQuery", "()Ljava/lang/String;");
            JENNY_CHECK_NULL(sMethod_getQuery_0);

            sMethod_getPath_0 = env->GetMethodID(sClazz, "getPath", "()Ljava/lang/String;");
            JENNY_CHECK_NULL(sMethod_getPath_0);

            sMethod_getUserInfo_0 = env->GetMethodID(sClazz, "getUserInfo", "()Ljava/lang/String;");
            JENNY_CHECK_NULL(sMethod_getUserInfo_0);

            sMethod_getAuthority_0 = env->GetMethodID(sClazz, "getAuthority", "()Ljava/lang/String;");
            JENNY_CHECK_NULL(sMethod_getAuthority_0);

            sMethod_getPort_0 = env->GetMethodID(sClazz, "getPort", "()I");
            JENNY_CHECK_NULL(sMethod_getPort_0);

            sMethod_getDefaultPort_0 = env->GetMethodID(sClazz, "getDefaultPort", "()I");
            JENNY_CHECK_NULL(sMethod_getDefaultPort_0);

            sMethod_getProtocol_0 = env->GetMethodID(sClazz, "getProtocol", "()Ljava/lang/String;");
            JENNY_CHECK_NULL(sMethod_getProtocol_0);

            sMethod_getHost_0 = env->GetMethodID(sClazz, "getHost", "()Ljava/lang/String;");
            JENNY_CHECK_NULL(sMethod_getHost_0);

            sMethod_getFile_0 = env->GetMethodID(sClazz, "getFile", "()Ljava/lang/String;");
            JENNY_CHECK_NULL(sMethod_getFile_0);

            sMethod_getRef_0 = env->GetMethodID(sClazz, "getRef", "()Ljava/lang/String;");
            JENNY_CHECK_NULL(sMethod_getRef_0);

            sMethod_equals_0 = env->GetMethodID(sClazz, "equals", "(Ljava/lang/Object;)Z");
            JENNY_CHECK_NULL(sMethod_equals_0);

            sMethod_hashCode_0 = env->GetMethodID(sClazz, "hashCode", "()I");
            JENNY_CHECK_NULL(sMethod_hashCode_0);

            sMethod_sameFile_0 = env->GetMethodID(sClazz, "sameFile", "(Ljava/net/URL;)Z");
            JENNY_CHECK_NULL(sMethod_sameFile_0);

            sMethod_toString_0 = env->GetMethodID(sClazz, "toString", "()Ljava/lang/String;");
            JENNY_CHECK_NULL(sMethod_toString_0);

            sMethod_toExternalForm_0 = env->GetMethodID(sClazz, "toExternalForm", "()Ljava/lang/String;");
            JENNY_CHECK_NULL(sMethod_toExternalForm_0);

            sMethod_toURI_0 = env->GetMethodID(sClazz, "toURI", "()Ljava/net/URI;");
            JENNY_CHECK_NULL(sMethod_toURI_0);

            sMethod_openConnection_0 = env->GetMethodID(sClazz, "openConnection", "()Ljava/net/URLConnection;");
            JENNY_CHECK_NULL(sMethod_openConnection_0);

            sMethod_openConnection_1 = env->GetMethodID(sClazz, "openConnection", "(Ljava/net/Proxy;)Ljava/net/URLConnection;");
            JENNY_CHECK_NULL(sMethod_openConnection_1);

            sMethod_openStream_0 = env->GetMethodID(sClazz, "openStream", "()Ljava/io/InputStream;");
            JENNY_CHECK_NULL(sMethod_openStream_0);

            sMethod_getContent_0 = env->GetMethodID(sClazz, "getContent", "()Ljava/lang/Object;");
            JENNY_CHECK_NULL(sMethod_getContent_0);

            sMethod_getContent_1 = env->GetMethodID(sClazz, "getContent", "([Ljava/lang/Class;)Ljava/lang/Object;");
            JENNY_CHECK_NULL(sMethod_getContent_1);

            sMethod_setURLStreamHandlerFactory_0 = env->GetStaticMethodID(sClazz, "setURLStreamHandlerFactory", "(Ljava/net/URLStreamHandlerFactory;)V");
            JENNY_CHECK_NULL(sMethod_setURLStreamHandlerFactory_0);



            sInited = true;
        }
    }
#undef JENNY_CHECK_NULL
   return true;
}

/*static*/ void URLProxy::releaseClazz(JNIEnv *env) {
    if (sInited) {
        std::lock_guard<std::mutex> lg(sInitLock);
        if (sInited) {
            env->DeleteGlobalRef(sClazz);
            sClazz = nullptr;
            sInited = false;
        }
    }
}

jmethodID URLProxy::sConstruct_0;
jmethodID URLProxy::sConstruct_1;
jmethodID URLProxy::sConstruct_2;
jmethodID URLProxy::sConstruct_3;
jmethodID URLProxy::sConstruct_4;
jmethodID URLProxy::sConstruct_5;

jmethodID URLProxy::sMethod_getQuery_0;
jmethodID URLProxy::sMethod_getPath_0;
jmethodID URLProxy::sMethod_getUserInfo_0;
jmethodID URLProxy::sMethod_getAuthority_0;
jmethodID URLProxy::sMethod_getPort_0;
jmethodID URLProxy::sMethod_getDefaultPort_0;
jmethodID URLProxy::sMethod_getProtocol_0;
jmethodID URLProxy::sMethod_getHost_0;
jmethodID URLProxy::sMethod_getFile_0;
jmethodID URLProxy::sMethod_getRef_0;
jmethodID URLProxy::sMethod_equals_0;
jmethodID URLProxy::sMethod_hashCode_0;
jmethodID URLProxy::sMethod_sameFile_0;
jmethodID URLProxy::sMethod_toString_0;
jmethodID URLProxy::sMethod_toExternalForm_0;
jmethodID URLProxy::sMethod_toURI_0;
jmethodID URLProxy::sMethod_openConnection_0;
jmethodID URLProxy::sMethod_openConnection_1;
jmethodID URLProxy::sMethod_openStream_0;
jmethodID URLProxy::sMethod_getContent_0;
jmethodID URLProxy::sMethod_getContent_1;
jmethodID URLProxy::sMethod_setURLStreamHandlerFactory_0;


} // endof namespace java
