package com.levylin.study.ffmpeg.live;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.levylin.study.ffmpeg.LogUtils;

/**
 * Created by LinXin on 2017/11/4.
 */

public class AudioPusher implements IPusher {

    private AudioParam param;
    private AudioRecord audioRecord;
    private int minBufferSize;
    private boolean isPushing;
    private PushNative pushNative;

    public AudioPusher(PushNative pushNative) {
        this.pushNative = pushNative;
        this.param = new AudioParam(44100, 1);
        int channelConfig = param.getChannel() == 1 ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO;
        minBufferSize = AudioRecord.getMinBufferSize(param.getSampleRateInHz(), channelConfig, AudioFormat.ENCODING_PCM_16BIT);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, param.getSampleRateInHz(), channelConfig, AudioFormat.ENCODING_PCM_16BIT, minBufferSize);
        pushNative.setAudioOptions(param.getSampleRateInHz(), param.getChannel());
    }

    @Override
    public void startPush() {
        isPushing = true;
        new Thread(new AudioRecordTask()).start();
    }

    @Override
    public void stopPush() {
        isPushing = false;
    }

    @Override
    public void release() {

    }

    private class AudioRecordTask implements Runnable {
        @Override
        public void run() {
            audioRecord.startRecording();
            while (isPushing) {
                byte[] buffer = new byte[minBufferSize];
                int len = audioRecord.read(buffer, 0, buffer.length);
                LogUtils.e("读取音频数据:len=" + len);
                if (len > 0) {
                    pushNative.pushAudio(buffer);
                }
            }
        }
    }
}
