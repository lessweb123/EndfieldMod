#include <jni.h>
#include <endfield_desktop_DesktopNativeHelper.h>

static jclass npe_class;

static jclass accessible_object_class;
static jfieldID override_field;

static jclass lookup_class;
static jfieldID impl_lookup_field;

static jclass reflects_class;
static jmethodID get_field_exception_method;
static jmethodID get_method_exception_method;
static jmethodID get_constructor_exception_method;

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void*)
{
    JNIEnv *env = nullptr;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_8) != JNI_OK) {
        return JNI_ERR;
    }

    npe_class = (jclass)env->NewGlobalRef(env->FindClass("java/lang/NullPointerException"));

    accessible_object_class = (jclass)env->NewGlobalRef(env->FindClass("java/lang/reflect/AccessibleObject"));
    override_field = env->GetFieldID(accessible_object_class, "override", "Z");

    lookup_class = (jclass)env->NewGlobalRef(env->FindClass("java/lang/invoke/MethodHandles$Lookup"));
    impl_lookup_field = env->GetStaticFieldID(lookup_class, "IMPL_LOOKUP", "Ljava/lang/invoke/MethodHandles$Lookup;");

    reflects_class = (jclass)env->NewGlobalRef(env->FindClass("endfield/util/Reflects"));
    get_field_exception_method = env->GetStaticMethodID(reflects_class, "getFieldException", "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/NoSuchFieldException;");
    get_method_exception_method = env->GetStaticMethodID(reflects_class, "getMethodException", "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/NoSuchMethodException;");
    get_constructor_exception_method = env->GetStaticMethodID(reflects_class, "getConstructorException", "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/NoSuchMethodException;");

    return JNI_VERSION_1_8;
}

JNIEXPORT jobject JNICALL Java_endfield_desktop_DesktopNativeHelper_getLookup(JNIEnv* env, jclass)
{
    return env->GetStaticObjectField(lookup_class, impl_lookup_field);
}

JNIEXPORT void JNICALL Java_endfield_desktop_DesktopNativeHelper_setAccessible(JNIEnv* env, jclass, jobject object, jboolean flag)
{
    if (override_field == nullptr)
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
    const char* csignature = env->GetStringUTFChars(signature, nullptr);

    if (cname == nullptr || csignature == nullptr)
    {
        if (cname) env->ReleaseStringUTFChars(name, cname);
        if (csignature) env->ReleaseStringUTFChars(signature, csignature);

        return nullptr;
    }

    jfieldID jfield_id = isStatic? env->GetStaticFieldID(clazz, cname, csignature): env->GetFieldID(clazz, cname, csignature);

    if (jfield_id == nullptr)
    {
        env->ExceptionClear();

        env->Throw((jthrowable)env->CallStaticObjectMethod(reflects_class, get_field_exception_method, clazz, name, signature));

        return nullptr;
    }

    jobject reflect_field = env->ToReflectedField(clazz, jfield_id, isStatic);

    env->ReleaseStringUTFChars(name, cname);
    env->ReleaseStringUTFChars(signature, csignature);

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
    const char* csignature = env->GetStringUTFChars(signature, nullptr);

    if (cname == nullptr || csignature == nullptr)
    {
        if (cname) env->ReleaseStringUTFChars(name, cname);
        if (csignature) env->ReleaseStringUTFChars(signature, csignature);

        return nullptr;
    }

    jmethodID jmethod_id = isStatic? env->GetStaticMethodID(clazz, cname, csignature): env->GetMethodID(clazz, cname, csignature);

    if (jmethod_id == nullptr)
    {
        env->ExceptionClear();

        env->Throw((jthrowable)env->CallStaticObjectMethod(reflects_class, get_method_exception_method, clazz, name, signature));

        return nullptr;
    }

    jobject reflect_method = env->ToReflectedMethod(clazz, jmethod_id, isStatic);

    env->ReleaseStringUTFChars(name, cname);
    env->ReleaseStringUTFChars(signature, csignature);

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

    if (jmethod_id == nullptr)
    {
        env->ExceptionClear();

        env->Throw((jthrowable)env->CallStaticObjectMethod(reflects_class, get_constructor_exception_method, clazz, signature));

        return nullptr;
    }

    jobject reflect_constructor = env->ToReflectedMethod(clazz, jmethod_id, JNI_FALSE);

    env->ReleaseStringUTFChars(signature, csignature);

    env->SetBooleanField(reflect_constructor, override_field, JNI_TRUE);

    return reflect_constructor;
}
