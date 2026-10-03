#include <cstring>
#include <jni.h>
#include <endfield_desktop_DesktopNativeHelper.h>

static jclass npe_class;
static jclass iae_class;

static jclass accessible_object_class;
static jfieldID override_field;

static jclass lookup_class;
static jfieldID impl_lookup_field;

static jclass reflects_class;
static jmethodID get_field_exception_method;
static jmethodID get_method_exception_method;
static jmethodID get_constructor_exception_method;

static bool initGlobalClassRef(JNIEnv* env, const char* name, jclass* out) {
    jclass local_ref = env->FindClass(name);
    if (!local_ref || env->ExceptionCheck())
        return false;
    *out = (jclass) env->NewGlobalRef(local_ref);
    env->DeleteLocalRef(local_ref);
    return *out != nullptr;
}

static bool initFieldID(JNIEnv* env, jclass clazz, const char* name, const char* sig, jfieldID* out) {
    *out = env->GetFieldID(clazz, name, sig);
    return *out != nullptr && !env->ExceptionCheck();
}

static bool initStaticFieldID(JNIEnv* env, jclass clazz, const char* name, const char* sig, jfieldID* out) {
    *out = env->GetStaticFieldID(clazz, name, sig);
    return *out != nullptr && !env->ExceptionCheck();
}

static bool initMethodID(JNIEnv* env, jclass clazz, const char* name, const char* sig, jmethodID* out) {
    *out = env->GetMethodID(clazz, name, sig);
    return *out != nullptr && !env->ExceptionCheck();
}

static bool initStaticMethodID(JNIEnv* env, jclass clazz, const char* name, const char* sig, jmethodID* out) {
    *out = env->GetStaticMethodID(clazz, name, sig);
    return *out != nullptr && !env->ExceptionCheck();
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void*)
{
    JNIEnv *env = nullptr;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_8) != JNI_OK) {
        return JNI_ERR;
    }

    bool ok = initGlobalClassRef(env, "java/lang/NullPointerException", &npe_class)
        && initGlobalClassRef(env, "java/lang/IllegalArgumentException", &iae_class)

        && initGlobalClassRef(env, "java/lang/reflect/AccessibleObject", &accessible_object_class)
        && initFieldID(env, accessible_object_class, "override", "Z", &override_field)

        && initGlobalClassRef(env, "java/lang/invoke/MethodHandles$Lookup", &lookup_class)
        && initStaticFieldID(env, lookup_class, "IMPL_LOOKUP", "Ljava/lang/invoke/MethodHandles$Lookup;",&impl_lookup_field)

        && initGlobalClassRef(env, "endfield/util/Reflects", &reflects_class)
        && initStaticMethodID(env, reflects_class, "getFieldException","(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/NoSuchFieldException;",&get_field_exception_method)
        && initStaticMethodID(env, reflects_class, "getMethodException","(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/NoSuchMethodException;",&get_method_exception_method)
        && initStaticMethodID(env, reflects_class, "getConstructorException","(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/NoSuchMethodException;",&get_constructor_exception_method);

    if (!ok)
    {
        if (env->ExceptionCheck())
            env->ExceptionClear();

        return JNI_ERR;
    }

    return JNI_VERSION_1_8;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM* vm, void*)
{
    JNIEnv* env = nullptr;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_8) != JNI_OK || env == nullptr) {
        // The calling thread is not attached to the JVM, so the global refs cannot be deleted. They are
        // released anyway when the JVM terminates.
        return;
    }

    env->DeleteGlobalRef(npe_class);
    env->DeleteGlobalRef(iae_class);
    env->DeleteGlobalRef(accessible_object_class);
    env->DeleteGlobalRef(lookup_class);
    env->DeleteGlobalRef(reflects_class);

    npe_class = nullptr;
    iae_class = nullptr;
    accessible_object_class = nullptr;
    lookup_class = nullptr;
    reflects_class = nullptr;

    override_field = nullptr;
    impl_lookup_field = nullptr;

    get_field_exception_method = nullptr;
    get_method_exception_method = nullptr;
    get_constructor_exception_method = nullptr;
}

JNIEXPORT jobject JNICALL Java_endfield_desktop_DesktopNativeHelper_getLookup(JNIEnv* env, jclass)
{
    return env->GetStaticObjectField(lookup_class, impl_lookup_field);
}

JNIEXPORT void JNICALL Java_endfield_desktop_DesktopNativeHelper_setAccessible(JNIEnv* env, jclass, jobject object, jboolean flag)
{
    if (object == nullptr)
    {
        env->ThrowNew(npe_class, "object is null");
        return;
    }

    env->SetBooleanField(object, override_field, flag);
}

JNIEXPORT jobject JNICALL Java_endfield_desktop_DesktopNativeHelper_getField(JNIEnv* env, jclass, jclass clazz, jstring name, jstring signature, jboolean isStatic)
{
    if (clazz == nullptr || name == nullptr || signature == nullptr)
    {
        env->ThrowNew(npe_class, "clazz/name/signature must not be null");
        return nullptr;
    }

    const char* cname = env->GetStringUTFChars(name, nullptr);

    if (cname == nullptr)
        return nullptr;

    const char* csignature = env->GetStringUTFChars(signature, nullptr);

    if (csignature == nullptr)
    {
        env->ReleaseStringUTFChars(name, cname);

        return nullptr;
    }

    jfieldID jfield_id = isStatic? env->GetStaticFieldID(clazz, cname, csignature): env->GetFieldID(clazz, cname, csignature);

    env->ReleaseStringUTFChars(name, cname);
    env->ReleaseStringUTFChars(signature, csignature);

    if (jfield_id == nullptr)
    {
        if (env->ExceptionCheck())
            env->ExceptionClear();

        env->Throw((jthrowable)env->CallStaticObjectMethod(reflects_class, get_field_exception_method, clazz, name, signature));

        return nullptr;
    }

    jobject reflect_field = env->ToReflectedField(clazz, jfield_id, isStatic);

    env->SetBooleanField(reflect_field, override_field, JNI_TRUE);

    return reflect_field;
}

JNIEXPORT jobject JNICALL Java_endfield_desktop_DesktopNativeHelper_getMethod(JNIEnv* env, jclass, jclass clazz, jstring name, jstring signature, jboolean isStatic)
{
    if (clazz == nullptr || name == nullptr || signature == nullptr)
    {
        env->ThrowNew(npe_class, "clazz/name/signature must not be null");
        return nullptr;
    }

    const char* cname = env->GetStringUTFChars(name, nullptr);

    if (cname == nullptr)
        return nullptr;

    const char* csignature = env->GetStringUTFChars(signature, nullptr);

    if (csignature == nullptr)
    {
        env->ReleaseStringUTFChars(name, cname);

        return nullptr;
    }

    if (strcmp(cname, "<init>") == 0)
    {
        env->ReleaseStringUTFChars(name, cname);
        env->ReleaseStringUTFChars(signature, csignature);

        env->ThrowNew(iae_class, "Not supporting finding '<init>' method");

        return nullptr;
    }

    jmethodID jmethod_id = isStatic ? env->GetStaticMethodID(clazz, cname, csignature): env->GetMethodID(clazz, cname, csignature);

    env->ReleaseStringUTFChars(name, cname);
    env->ReleaseStringUTFChars(signature, csignature);

    if (jmethod_id == nullptr)
    {
        if (env->ExceptionCheck())
            env->ExceptionClear();

        env->Throw((jthrowable)env->CallStaticObjectMethod(reflects_class, get_method_exception_method, clazz, name, signature));

        return nullptr;
    }

    jobject reflect_method = env->ToReflectedMethod(clazz, jmethod_id, isStatic);

    env->SetBooleanField(reflect_method, override_field, JNI_TRUE);

    return reflect_method;
}

JNIEXPORT jobject JNICALL Java_endfield_desktop_DesktopNativeHelper_getConstructor(JNIEnv* env, jclass, jclass clazz, jstring signature)
{
    if (clazz == nullptr || signature == nullptr)
    {
        env->ThrowNew(npe_class, "clazz/signature must not be null");
        return nullptr;
    }

    const char* csignature = env->GetStringUTFChars(signature, nullptr);

    if (csignature == nullptr)
        return nullptr;

    jmethodID jmethod_id = env->GetMethodID(clazz, "<init>", csignature);

    env->ReleaseStringUTFChars(signature, csignature);

    if (jmethod_id == nullptr)
    {
        if (env->ExceptionCheck())
            env->ExceptionClear();

        env->Throw((jthrowable)env->CallStaticObjectMethod(reflects_class, get_constructor_exception_method, clazz, signature));

        return nullptr;
    }

    jobject reflect_constructor = env->ToReflectedMethod(clazz, jmethod_id, JNI_FALSE);

    env->SetBooleanField(reflect_constructor, override_field, JNI_TRUE);

    return reflect_constructor;
}
