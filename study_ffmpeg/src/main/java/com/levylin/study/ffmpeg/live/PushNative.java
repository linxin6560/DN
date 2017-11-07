package com.levylin.study.ffmpeg.live;

/**
 * Created by LinXin on 2017/11/5.
 */
public class PushNative {

    /**
     * 设置视频参数
     */
    public native void setVideoOptions(int width, int height, int bitrate, int fps);

    /**
     * 设置音频数据
     */
    public native void setAudioOptions(int sampleRate, int channel);

    /**
     * 推流视频
     *
     * @param data
     */
    public native void pushVideo(byte[] data);

    /**
     * 推流音频
     *
     * @param data
     */
    public native void pushAudio(byte[] data);

    public native void release();

    public native void stopPush();

    public native void startPush(String url);
}
