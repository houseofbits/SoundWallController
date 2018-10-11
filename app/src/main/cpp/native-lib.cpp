#include <jni.h>
#include <string>

std::string consoleStr = "";

void addConsoleLine(std::string str){
    consoleStr = consoleStr + "JNI: "+str + "\n";
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_soundwallcontroller_MainActivity_getDebugStringJNI(
        JNIEnv* env,
        jobject) {
    std::string str = consoleStr;
    consoleStr.empty();
    return env->NewStringUTF(str.c_str());
}

//CRC-8 - based on the CRC8 formulas by Dallas/Maxim
//code released under the therms of the GNU GPL 3.0 license
extern "C" JNIEXPORT jint JNICALL
Java_com_soundwallcontroller_MainActivity_CRC8JNI(
        JNIEnv* env,
        jobject,
        jintArray jData,
        jint length) {

    unsigned char data[length];
    jint *getints = env->GetIntArrayElements(jData, NULL);
    for (int i = 0; i < length; i++) {
        int val = getints[i];
        if(val<0)val = 0;
        if(val>255)val = 255;
        data[i] = (unsigned char)val;
    }
    unsigned char crc = 0x00;
    unsigned char len = (unsigned char)length;
    int cnt=0;
    while (len--) {
        unsigned char extract = data[cnt];  //(*data)++;
        cnt++;
        for (unsigned char tempI = 8; tempI; tempI--) {
            unsigned char sum = (crc ^ extract) & 0x01;
            crc >>= 1;
            if (sum) {
                crc ^= 0x8C;
            }
            extract >>= 1;
        }
    }
    return (int)crc;
}
