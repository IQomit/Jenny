/**
 * File generated by Jenny -- https://github.com/LanderlYoung/Jenny
 *
 * DO NOT EDIT THIS FILE.
 *
 * For bug report, please refer to github issue tracker https://github.com/LanderlYoung/Jenny/issues,
 * or contact author landerlyoung@gmail.com.
 */
#pragma once

#include <jni.h>
#include <assert.h>                        
#include <atomic>
#include <mutex>


class ComputeInNativeProxy {

public:
    static constexpr auto FULL_CLASS_NAME = "io/github/landerlyoung/jennysample/ComputeInNative";



private:
    // thread safe init
    static std::atomic_bool sInited;
    static std::mutex sInitLock;

    JNIEnv* mJniEnv;
    jobject mJavaObjectReference;

public:

    static bool initClazz(JNIEnv *env);
    
    static void releaseClazz(JNIEnv *env);

    static void assertInited(JNIEnv *env) {
        assert(initClazz(env));
    }

    ComputeInNativeProxy(JNIEnv *env, jobject javaObj)
            : mJniEnv(env), mJavaObjectReference(javaObj) {
        assertInited(env);
    }

    ComputeInNativeProxy(const ComputeInNativeProxy &from) = default;
    ComputeInNativeProxy &operator=(const ComputeInNativeProxy &) = default;

    ComputeInNativeProxy(ComputeInNativeProxy &&from)
           : mJniEnv(from.mJniEnv), mJavaObjectReference(from.mJavaObjectReference) {
        from.mJavaObjectReference = nullptr;
    }

    ~ComputeInNativeProxy() = default;
    
    // helper method to get underlay jobject reference
    jobject operator*() {
       return mJavaObjectReference;
    }
    
    // helper method to delete JNI local ref.
    // use only when you really understand JNIEnv::DeleteLocalRef.
    void deleteLocalRef() {
       if (mJavaObjectReference) {
           mJniEnv->DeleteLocalRef(mJavaObjectReference);
           mJavaObjectReference = nullptr;
       }
    }
    
    // === java methods below ===
    


    // field: private long nativeContext
    jlong getNativeContext() const {
       
       return mJniEnv->GetLongField(mJavaObjectReference, sField_nativeContext_0);

    }

    // field: private long nativeContext
    void setNativeContext(jlong nativeContext) const {
        
        mJniEnv->SetLongField(mJavaObjectReference, sField_nativeContext_0, nativeContext);
    }



private:
    static jclass sClazz;


    static jfieldID sField_nativeContext_0;

};

