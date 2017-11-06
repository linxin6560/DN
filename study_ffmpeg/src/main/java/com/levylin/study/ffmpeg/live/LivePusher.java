package com.levylin.study.ffmpeg.live;

import android.app.Activity;
import android.view.SurfaceHolder;

/**
 * 直播推流
 * Created by LinXin on 2017/11/5.
 */
public class LivePusher {

    private VideoPusher videoPusher;
    private AudioPusher audioPusher;
    private PushNative mNative;
    private LiveStateChangeListener mListener;
    private Activity activity;
    private boolean isPushing = false;

    public LivePusher(Activity activity) {
        this.activity = activity;
        mNative = new PushNative();
    }

    public void prepare(SurfaceHolder holder) {
        videoPusher = new VideoPusher(activity, holder, mNative);
        audioPusher = new AudioPusher(mNative);
        videoPusher.setLiveStateChangeListener(mListener);
        audioPusher.setLiveStateChangeListener(mListener);
    }


    public void startPusher(String url) {
        isPushing = true;
        videoPusher.startPush();
        audioPusher.startPush();
        mNative.startPush(url);
    }

    public void stopPusher() {
        isPushing = false;
        videoPusher.stopPush();
        audioPusher.stopPush();
        mNative.stopPush();
    }

    public void switchCamera() {
        videoPusher.switchCamera();
    }

    public void relase() {
        stopPusher();
        videoPusher.setLiveStateChangeListener(null);
        audioPusher.setLiveStateChangeListener(null);
        mNative.setLiveStateChangeListener(null);
        videoPusher.release();
        audioPusher.release();
        mNative.release();
    }

    public boolean isPushing() {
        return isPushing;
    }

    public void setLiveStateChangeListener(LiveStateChangeListener listener) {
        mListener = listener;
        mNative.setLiveStateChangeListener(listener);
        if (null != videoPusher) {
            videoPusher.setLiveStateChangeListener(listener);
        }
        if (null != audioPusher) {
            audioPusher.setLiveStateChangeListener(listener);
        }

    }
}
