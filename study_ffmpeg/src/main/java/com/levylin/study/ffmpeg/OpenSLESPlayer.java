package com.levylin.study.ffmpeg;

/**
 * Created by LinXin on 2017/10/12.
 */
public class OpenSLESPlayer {


    public native void play(String path);

    public native void shutdown();
}
