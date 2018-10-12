//
// Created by KristsPudzens on 12.10.2018.
//

#ifndef SOUNDWALLCONTROLLER_SOUNDSYSTEM_H
#define SOUNDWALLCONTROLLER_SOUNDSYSTEM_H

#include <jni.h>
#include <string>

// Beautifying the ugly Java-C++ bridge (JNI) with these macros.
#define MAKE_JNI_FUNCTION(r, p, n) extern "C" JNIEXPORT r JNICALL Java_com_soundwallcontroller_ ## p ## _ ## n
#define JNI(r, p, n) MAKE_JNI_FUNCTION(r, p, n)

void addConsoleLine(std::string str);



#endif //SOUNDWALLCONTROLLER_SOUNDSYSTEM_H
