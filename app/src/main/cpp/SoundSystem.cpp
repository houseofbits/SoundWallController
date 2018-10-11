//
// Created by KristsPudzens on 11.10.2018.
//

#include <jni.h>
#include <string>
#include <android/log.h>
#include <AndroidIO/SuperpoweredAndroidAudioIO.h>
#include <AndroidIO/SuperpoweredUSBAudio.h>
#include <SuperpoweredAdvancedAudioPlayer.h>
#include <SuperpoweredSimple.h>
#include <SuperpoweredCPU.h>
#include <malloc.h>
#include <SLES/OpenSLES_AndroidConfiguration.h>
#include <SLES/OpenSLES.h>

static SuperpoweredAndroidAudioIO *audioIO;
static SuperpoweredAdvancedAudioPlayer *player;
static float *floatBuffer;


// This is called periodically by the audio engine.
static bool audioProcessing (
        void * __unused clientdata, // custom pointer
        short int *audio,           // buffer of interleaved samples
        int numberOfFrames,         // number of frames to process
        int __unused samplerate     // sampling rate
) {
    if (player->process(floatBuffer, false, (unsigned int)numberOfFrames)) {
        SuperpoweredFloatToShortInt(floatBuffer, audio, (unsigned int)numberOfFrames);
        return true;
    } else {
        return false;
    }
}

// Called by the player.
static void playerEventCallback (
        void * __unused clientData,
        SuperpoweredAdvancedAudioPlayerEvent event,
        void *value
) {
    switch (event) {
        case SuperpoweredAdvancedAudioPlayerEvent_LoadSuccess:
            break;
        case SuperpoweredAdvancedAudioPlayerEvent_LoadError:
//            log_print(ANDROID_LOG_ERROR, "PlayerExample", "Open error: %s", (char *)value);
            break;
        case SuperpoweredAdvancedAudioPlayerEvent_EOF:
            player->seek(0);    // loop track
            break;
        default:;
    };
}

// StartAudio - Start audio engine and initialize player.
extern "C" JNIEXPORT void
Java_com_soundwallcontroller_MainActivity_StartAudio (
        JNIEnv * __unused env,
        jobject  __unused obj,
        jint samplerate,
        jint buffersize
) {
    // Allocate audio buffer.
    floatBuffer = (float *)malloc(sizeof(float) * 2 * buffersize);

    // Initialize player and pass callback function.
    player = new SuperpoweredAdvancedAudioPlayer (
            NULL,                           // clientData
            playerEventCallback,            // callback function
            (unsigned int)samplerate,       // sampling rate
            0                               // cachedPointCount
    );

    // Initialize audio with audio callback function.
    audioIO = new SuperpoweredAndroidAudioIO (
            samplerate,                     // sampling rate
            buffersize,                     // buffer size
            false,                          // enableInput
            true,                           // enableOutput
            audioProcessing,                // process callback function
            NULL,                           // clientData
            -1,                             // inputStreamType (-1 = default)
            SL_ANDROID_STREAM_MEDIA,        // outputStreamType (-1 = default)
            buffersize * 2                  // latencySamples
    );
}

// OpenFile - Open file in player, specifying offset and length.
extern "C" JNIEXPORT void
Java_com_soundwallcontroller_MainActivity_OpenFile (
        JNIEnv *env,
        jobject __unused obj,
        jstring path,       // path to APK file
        jint offset,        // offset of audio file
        jint length         // length of audio file
) {
    const char *str = env->GetStringUTFChars(path, 0);
    player->open(str, offset, length);
    env->ReleaseStringUTFChars(path, str);
}

// TogglePlayback - Toggle Play/Pause state of the player.
extern "C" JNIEXPORT void
Java_com_soundwallcontroller_MainActivity_TogglePlayback (
        JNIEnv * __unused env,
        jobject __unused obj
) {
    player->togglePlayback();
    SuperpoweredCPU::setSustainedPerformanceMode(player->playing);  // prevent dropouts
}

// onBackground - Put audio processing to sleep.
extern "C" JNIEXPORT void
Java_com_soundwallcontroller_MainActivity_onBackground (
        JNIEnv * __unused env,
        jobject __unused obj
) {
    audioIO->onBackground();
}

// onForeground - Resume audio processing.
extern "C" JNIEXPORT void
Java_com_soundwallcontroller_MainActivity_onForeground (
        JNIEnv * __unused env,
        jobject __unused obj
) {
    audioIO->onForeground();
}

// Cleanup - Free resources.
extern "C" JNIEXPORT void
Java_com_soundwallcontroller_MainActivity_Cleanup (
        JNIEnv * __unused env,
        jobject __unused obj
) {
    delete audioIO;
    delete player;
    free(floatBuffer);
}


// Beautifying the ugly Java-C++ bridge (JNI) with these macros.
#define PID com_soundwallcontroller_SuperpoweredUSBAudio // Java package name and class name. Don't forget to update when you copy this code.
#define MAKE_JNI_FUNCTION(r, n, p) extern "C" JNIEXPORT r JNICALL Java_ ## p ## _ ## n
#define JNI(r, n, p) MAKE_JNI_FUNCTION(r, n, p)

// This is called by the SuperpoweredUSBAudio Java object when a USB device is connected.
JNI(jint, onConnect, PID) (
        JNIEnv *env,
        jobject __unused obj,
        jint deviceID,
        jint fd,
        jbyteArray rawDescriptor
) {
    jbyte *rd = env->GetByteArrayElements(rawDescriptor, NULL);
    int dataBytes = env->GetArrayLength(rawDescriptor);
    int r = SuperpoweredUSBSystem::onConnect(deviceID, fd, (unsigned char *)rd, dataBytes);
    env->ReleaseByteArrayElements(rawDescriptor, rd, JNI_ABORT);

    // r is 0 if SuperpoweredUSBSystem can't do anything with the connected device.
    // r & 2 is true if the device has MIDI. Start receiving events.
    //if (r & 2) SuperpoweredUSBMIDI::startIO(deviceID, NULL, onMidiReceived);

    // r & 1 is true if the device has audio. Start output.
    if (r & 1) {
        // allocate struct for sine wave oscillator
//        sineWaveOutput *swo = (sineWaveOutput *)malloc(sizeof(sineWaveOutput));
//        if (swo) {
//            swo->mul = 0.0f;
//            swo->step = 0;
//            SuperpoweredCPU::setSustainedPerformanceMode(true);
//
//            // Our preferred settings: 44100 Hz, 16 bits, 0 input channels, 256 output channels,
//            // low latency. Superpowered will set up the audio device as close as it can to these.
//            SuperpoweredUSBAudio::easyIO (
//                    deviceID,                    // deviceID
//                    44100,                       // sampling rate
//                    16,                          // bits per sample
//                    0,                           // numInputChannels
//                    256,                         // numOutputChannels
//                    SuperpoweredUSBLatency_Low,  // latency
//                    swo,                         // clientData
//                    audioProcessing              // SuperpoweredUSBAudioProcessingCallback
//            );
//        }
    }
    return r;
}

// This is called by the SuperpoweredUSBAudio Java object when a USB device is disconnected.
JNI(void, onDisconnect, PID) (
        JNIEnv * __unused env,
        jobject __unused obj,
        jint deviceID
) {
    SuperpoweredUSBSystem::onDisconnect(deviceID);
    SuperpoweredCPU::setSustainedPerformanceMode(false);
}