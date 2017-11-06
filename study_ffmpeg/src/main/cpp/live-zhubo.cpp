//
// Created by Administrator on 2017/10/29.
//
#include <jni.h>
#include <malloc.h>
#include "my_log.h"
#include <queue>
#include <pthread.h>

#include "libx264/x264.h"
#include "librtmp/rtmp.h"
#include "librtmp/rtmp_sys.h"

pthread_mutex_t mutex;
pthread_cond_t cond;
using namespace std;
queue<RTMPPacket *> rtmp_queue;
int isPushing;

x264_picture_t *pic_in;
x264_picture_t *pic_out;
int y_len;
int u_len;
int v_len;
x264_t *video_encoder;
long start_time;
char *rtmp_path;

void add_264_sequence_header(unsigned char sps[100], unsigned char pps[100], int len, int pps_len);

void add_rtmp_packet(RTMPPacket *pPacket);

void add_264_body(uint8_t *payload, int i_payload);

/**
 * 发送头信息
 * @param sps
 * @param pps
 * @param len
 * @param pps_len
 */
void add_264_sequence_header(unsigned char *sps, unsigned char *pps, int sps_len, int pps_len) {
    int body_size = sps_len + pps_len + 16;
    LOGE("add_264_sequence_header....body_size=%d", body_size);
    RTMPPacket *packet = (RTMPPacket *) malloc(sizeof(RTMPPacket));
    RTMPPacket_Alloc(packet, body_size);
    RTMPPacket_Reset(packet);
    char *body = packet->m_body;
    int i = 0;
    body[i++] = 0x17;
    body[i++] = 0x00;
    //composition time 0x000000
    body[i++] = 0x00;
    body[i++] = 0x00;
    body[i++] = 0x00;
    /*AVCDecoderConfigurationRecord*/
    body[i++] = 0x01;
    body[i++] = sps[1];//profile
    body[i++] = sps[2];//兼容性
    body[i++] = sps[3];//baseline
    body[i++] = 0xFF;
    /*sps*/
    body[i++] = 0xE1;
    body[i++] = (sps_len >> 8) & 0xFF;//int 的低16位放到body中
    body[i++] = sps_len & 0xFF;
    memcpy(&body[i], sps, sps_len);//sps的内容
    i += sps_len;//偏移量加上sps的长度

    //pps的长度
    body[i++] = 0x01;
    body[i++] = (pps_len >> 8) & 0xFF;
    body[i++] = pps_len & 0xFF;
    memcpy(&body[i], pps, pps_len);//pps的内容
    i += pps_len;
    //packet参数设置
    packet->m_packetType = RTMP_PACKET_TYPE_VIDEO;
    packet->m_nBodySize = body_size;
    packet->m_nChannel = 0x04;
    packet->m_nTimeStamp = 0;
    packet->m_hasAbsTimestamp = 0;
    packet->m_headerType = RTMP_PACKET_SIZE_MEDIUM;//注意不要写成RTMP_PACKET_SIZE_MINIMUM，否则会崩溃
    add_rtmp_packet(packet);
}

void add_264_body(uint8_t *buffer, int len) {
    if (buffer[2] == 0x00) {
        buffer += 4;//首地址加4
        len -= 4;
    } else if (buffer[2] == 0x01) {
        buffer += 3;
        len -= 3;
    }
    int body_size = len + 9;
    RTMPPacket *packet = (RTMPPacket *) malloc(sizeof(RTMPPacket));
    RTMPPacket_Alloc(packet, body_size);
    char *body = packet->m_body;
    //判断关键帧和非关键帧
    int type = buffer[0] & 0x17;
    LOGE("buffer[0]:%d,type:%d", buffer[0], type);
    if (type == NAL_SLICE_IDR) {
        body[0] = 0x17;
    } else {
        body[0] = 0x27;
    }
    body[1] = 0x01;
    body[2] = 0x00;
    body[3] = 0x00;
    body[4] = 0x00;
    //写入数据长度
    body[5] = (len >> 24) & 0xFF;
    body[6] = (len >> 16) & 0xFF;
    body[7] = (len >> 8) & 0xFF;
    body[8] = len & 0xFF;
    //写入数据
    memcpy(&body[9], buffer, len);
    //packet参数设置
    packet->m_packetType = RTMP_PACKET_TYPE_VIDEO;
    packet->m_nBodySize = body_size;
    packet->m_nTimeStamp = RTMP_GetTime() - start_time;
    packet->m_hasAbsTimestamp = 0;
    packet->m_nChannel = 0x04;
    packet->m_headerType = RTMP_PACKET_SIZE_LARGE;
    add_rtmp_packet(packet);
}

void add_rtmp_packet(RTMPPacket *pPacket) {
    LOGE("插入一个RTMP包");
    pthread_mutex_lock(&mutex);
    if (isPushing) {
        rtmp_queue.push(pPacket);
    }
    pthread_cond_signal(&cond);
    pthread_mutex_unlock(&mutex);
}

RTMPPacket *get_rtmp_packet() {
    pthread_mutex_lock(&mutex);
    if (rtmp_queue.empty()) {
        pthread_cond_wait(&cond, &mutex);
    }
    RTMPPacket *packet = rtmp_queue.front();
    rtmp_queue.pop();
    pthread_mutex_unlock(&mutex);
    LOGE("获取到一个RTMP包");
    return packet;
}

void *push_thread(void *data) {
    isPushing = 1;
    LOGE("push_thread");
    RTMP *rtmp = RTMP_Alloc();
    LOGE("RTMP_Alloc success");
    RTMP_Init(rtmp);
    LOGE("RTMP_Init success");
    rtmp->Link.timeout = 5;
    RTMP_SetupURL(rtmp, rtmp_path);
    LOGE("RTMP_SetupURL success RTMP is :%d path:%s", rtmp ? 1 : 0, rtmp_path);
    RTMP_EnableWrite(rtmp);
    LOGE("RTMP_EnableWrite success");
    if (!RTMP_Connect(rtmp, NULL)) {
        LOGE("RTMP_Connect failed");
    } else {
        LOGE("RTMP_Connect success");
        RTMP_ConnectStream(rtmp, 0);
        LOGE("RTMP_ConnectStream() success!");
        while (isPushing) {
            RTMPPacket *packet = get_rtmp_packet();
            packet->m_nInfoField2 = rtmp->m_stream_id;
            LOGE("设置m_nInfoField2为:%d", packet->m_nInfoField2);
            int result = RTMP_SendPacket(rtmp, packet, TRUE); /*TRUE为放进发送队列,FALSE是不放进发送队列,直接发送*/
            LOGE("循环发送视频包....result=%d", result);
            RTMPPacket_Free(packet);
            free(packet);
        }
    }
    isPushing = 0;
    free(rtmp_path);
    RTMP_Close(rtmp);
    RTMP_Free(rtmp);
    pthread_exit(0);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_levylin_study_ffmpeg_live_PushNative_startPush(JNIEnv *env, jobject instance,
                                                        jstring url_) {
    const char *url = env->GetStringUTFChars(url_, 0);
    rtmp_path = (char *) malloc(strlen(url) + 1);
    memset(rtmp_path, 0, strlen(url) + 1);
    memcpy(rtmp_path, url, strlen(url));
    start_time = RTMP_GetTime();
    pthread_mutex_init(&mutex, NULL);
    pthread_cond_init(&cond, NULL);
    pthread_t push_tid;
    pthread_create(&push_tid, NULL, push_thread, NULL);

    env->ReleaseStringUTFChars(url_, url);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_levylin_study_ffmpeg_live_PushNative_setVideoOptions(JNIEnv *env, jobject instance,
                                                              jint width, jint height, jint bitrate,
                                                              jint fps) {
    LOGE("width=%d,hgith=%d,bitrate=%d,fps=%d", width, height, bitrate, fps);
    y_len = width * height;
    u_len = y_len / 4;
    v_len = u_len;
    //x264的参数
    x264_param_t param;
    LOGE("设置参数");
    x264_param_default_preset(&param, x264_preset_names[0], x264_tune_names[7]);
    param.i_level_idc = 51;
    param.i_csp = X264_CSP_I420;
    param.i_width = width;
    param.i_height = height;
    param.i_threads = 1;
    param.i_fps_num = (uint32_t) fps;
    param.i_fps_den = 1;
    param.i_timebase_num = param.i_fps_num;
    param.i_timebase_den = param.i_fps_den;
    param.i_keyint_max = fps * 2;//关键帧间隔时间的帧率
    param.rc.i_rc_method = X264_RC_ABR;//平均码率.......CQP(恒定质量),CRF(恒定码率)
    param.rc.i_bitrate = bitrate / 1000;
    param.rc.i_vbv_max_bitrate = (int) (param.rc.i_bitrate * 1.2);
    param.rc.i_vbv_buffer_size = param.rc.i_bitrate;
    //设置输入 0 pts 来做音频同步
    param.b_vfr_input = 0;
    param.b_repeat_headers = 1;//SPS PPS 重要
    LOGE("设置画面质量");
    /**
     *Baseline（最低Profile）级别支持I/P 帧，只支持无交错（Progressive）和CAVLC，一般用于低阶或需要额外容错的应用，比如视频通话、手机视频等；
     *Main（主要Profile）级别提供I/P/B 帧，支持无交错（Progressive）和交错（Interlaced），同样提供对于CAVLC 和CABAC 的支持，用于主流消费类电子产品规格如低解码（相对而言）的mp4、便携的视频播放器、PSP和Ipod等；
     *High（高端Profile，也叫FRExt）级别在Main的基础上增加了8x8 内部预测、自定义量化、无损视频编码和更多的YUV 格式（如4：4：4）用于广播及视频碟片存储（蓝光影片），高清电视的应用。
     */
    x264_param_apply_profile(&param, x264_profile_names[0]);

    //打开编码器
    video_encoder = x264_encoder_open_152(&param);

    if (!video_encoder) {
        LOGE("打开失败");
        return;
    }
    pic_in = (x264_picture_t *) malloc(sizeof(x264_picture_t));
    pic_out = (x264_picture_t *) malloc(sizeof(x264_picture_t));
    x264_picture_alloc(pic_in, X264_CSP_I420, width, height);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_levylin_study_ffmpeg_live_PushNative_pushVideo(JNIEnv *env, jobject instance,
                                                        jbyteArray data_) {
    jbyte *data = env->GetByteArrayElements(data_, NULL);
    LOGE("pushVideo");
    jbyte *u = (jbyte *) pic_in->img.plane[1];
    jbyte *v = (jbyte *) pic_in->img.plane[2];
    memcpy(pic_in->img.plane[0], data, y_len);//不写这句话，只会看见黑屏，因为没数据
    for (int i = 0; i < u_len; ++i) {
        *(u + i) = *(data + y_len + i * 2 + 1);
        *(v + i) = *(data + y_len + 1 * 2);
    }

    x264_nal_t *nal = NULL;
    int n_nal = -1;
    if (x264_encoder_encode(video_encoder, &nal, &n_nal, pic_in, pic_out) < 0) {
        LOGE("编码失败");
        return;
    }

    unsigned char sps[100];
    unsigned char pps[100];
    int sps_len;
    int pps_len;
    for (int i = 0; i < n_nal; ++i) {
        if (nal[i].i_type == NAL_SPS) {
            LOGE("关键帧信息");
            sps_len = nal[i].i_payload - 4;
            memcpy(sps, nal[i].p_payload + 4, sps_len);
        } else if (nal[i].i_type == NAL_PPS) {
            LOGE("P帧信息");
            pps_len = nal[i].i_payload - 4;
            memcpy(pps, nal[i].p_payload + 4, pps_len);
            add_264_sequence_header(sps, pps, sps_len, pps_len);
        } else {
            LOGE("普通帧...");
            add_264_body(nal[i].p_payload, nal[i].i_payload);
        }
    }
    env->ReleaseByteArrayElements(data_, data, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_levylin_study_ffmpeg_live_PushNative_release(JNIEnv *env, jobject instance) {
    LOGE("release");
    isPushing = 0;
    free(pic_in);
    free(pic_out);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_levylin_study_ffmpeg_live_PushNative_stopPush(JNIEnv *env, jobject instance) {
    LOGE("stopPush");
    isPushing = 0;
}