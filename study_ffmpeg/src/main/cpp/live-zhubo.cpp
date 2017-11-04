//
// Created by Administrator on 2017/10/29.
//
#include <jni.h>
#include "my_log.h"

extern "C" {
#include "libx264/x264.h"
#include "libfaac/faac.h"
#include "librtmp/rtmp.h"
#include "librtmp/rtmp_sys.h"
}

extern "C"
JNIEXPORT void JNICALL
Java_com_levylin_study_1ffmpeg_ZhuBo_test(JNIEnv *env, jobject instance) {

    LOGE("ZhuBo test");
    x264_picture_t *pic;
    x264_picture_init(pic);
    LOGE("x264 ok");

    faacEncGetVersion(NULL, NULL);
    LOGE("faac ok");

    RTMP_Alloc();
    LOGE("rtmp ok");
}