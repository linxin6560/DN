package com.levylin.study.ffmpeg.live;

import com.levylin.study.ffmpeg.LogUtils;

/**
 * Created by LinXin on 2017/11/5.
 */

public class PushNative {

    private LiveStateChangeListener mListener;

    /**
     * 设置视频参数
     */
    public native void setVideoOptions(int width, int height, int bitrate, int fps);

    /**
     * 推流视频
     *
     * @param data
     */
    public native void pushVideo(byte[] data);

    public void onPostNativeError(int code) {
        LogUtils.e(code + "");
        if (null != mListener) {
            mListener.onErrorPusher(code);
        }
    }

    public void onPostNativeState(int state) {
        if (state == 100) {
            mListener.onStartPusher();
        } else if (state == 101) {
            mListener.onStopPusher();
        }
    }

    public void setLiveStateChangeListener(LiveStateChangeListener mListener) {
        this.mListener = mListener;
    }

    public native void release();

    public native void stopPush();

    public native void startPush(String url);
}
