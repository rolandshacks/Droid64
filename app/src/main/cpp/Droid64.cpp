#include <stdlib.h>
#include <memory.h>
#include <jni.h>

#include "./emu/frodo.h"

static const int rawVideoBufferSize = 0x180 * 0x110 * 4;
static void* rawVideoBuffer = NULL;

static const int rawAudioSamplesPerFrame = 44100 / 50; // audio sample rate / 50 Hz
static const int rawAudioBufferSize = rawAudioSamplesPerFrame * 2; // samples * 16bit mono
static void* rawAudioBuffer = NULL;

extern "C"
{

JNIEXPORT jint JNICALL Java_org_codewiz_droid64_emu_EmuBindings_init(JNIEnv* env, jobject obj, jstring prefs, jint flags)
{
    if (NULL == rawVideoBuffer)
    {
        rawVideoBuffer = new unsigned char[rawVideoBufferSize];
    }

    if (NULL == rawAudioBuffer)
    {
        rawAudioBuffer = new unsigned char[rawAudioBufferSize];
    }

    const char *nativeString = env->GetStringUTFChars(prefs, 0);

    int result = emu_init(nativeString, (int) flags);

    env->ReleaseStringUTFChars(prefs, nativeString);

    return result;
}

JNIEXPORT jint JNICALL Java_org_codewiz_droid64_emu_EmuBindings_input(JNIEnv* env, jobject obj, jint keyCode)
{
    return emu_input((int) keyCode);
}

JNIEXPORT jint JNICALL Java_org_codewiz_droid64_emu_EmuBindings_load(JNIEnv* env, jobject obj,
                                                                 jint dataType,
                                                                 jbyteArray data,
                                                                 jint dataSize,
                                                                 jstring filename)
{
    jboolean isCopy;
    jbyte* rawjBytes = env->GetByteArrayElements(data, &isCopy);
    const char *nativeString = env->GetStringUTFChars(filename, 0);

    int result = emu_load((int) dataType, rawjBytes, (int) dataSize, nativeString);

    env->ReleaseByteArrayElements(data, rawjBytes, 0);
    env->ReleaseStringUTFChars(filename, nativeString);

    return result;
}

JNIEXPORT jint JNICALL Java_org_codewiz_droid64_emu_EmuBindings_store(JNIEnv* env, jobject obj,
                                                                  jint dataType,
                                                                  jbyteArray data,
                                                                  jint dataSize)
{
    jboolean isCopy;
    jbyte* rawjBytes = env->GetByteArrayElements(data, &isCopy);

    int result = emu_store((int) dataType, (void*) rawjBytes, (int) dataSize);

    env->ReleaseByteArrayElements(data, rawjBytes, 0);

    return result;
}

JNIEXPORT jint JNICALL Java_org_codewiz_droid64_emu_EmuBindings_command(JNIEnv* env, jobject obj, jint command)
{
    return emu_command((int) command);
}

JNIEXPORT jint JNICALL Java_org_codewiz_droid64_emu_EmuBindings_update(JNIEnv* env, jobject obj,
                                                                   jint joystickInput,
                                                                   jbyteArray videoOutput,
                                                                   jbyteArray audioOutput,
                                                                   int flags)
{
    int result = emu_update((int) joystickInput, (void*) rawVideoBuffer, (void*) rawAudioBuffer, (int) flags);

    // update graphics after vblank flag
    if (1 == result)
    {
        env->SetByteArrayRegion(videoOutput, 0, (jsize) rawVideoBufferSize, (const jbyte*) rawVideoBuffer);
        env->SetByteArrayRegion(audioOutput, 0, (jsize) rawAudioBufferSize, (const jbyte*) rawAudioBuffer);
    }

    return result;
}

JNIEXPORT jint JNICALL Java_org_codewiz_droid64_emu_EmuBindings_shutdown(JNIEnv* env, jobject obj)
{
    if (rawVideoBuffer)
    {
        delete [] (unsigned char*) rawVideoBuffer;
        rawVideoBuffer = NULL;
    }

    if (rawAudioBuffer)
    {
        delete [] (unsigned char*) rawAudioBuffer;
        rawAudioBuffer = NULL;
    }

    int result = emu_shutdown();

    return result;
}

} // extern "C"
