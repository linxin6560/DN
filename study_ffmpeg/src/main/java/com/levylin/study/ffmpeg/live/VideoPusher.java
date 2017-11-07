package com.levylin.study.ffmpeg.live;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.levylin.study.ffmpeg.LogUtils;
import com.levylin.study.ffmpeg.MyAppliacation;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * 主播摄像头
 * Created by LinXin on 2017/10/29.
 */

public class VideoPusher implements Camera.PreviewCallback, SurfaceHolder.Callback, IPusher {

    private Activity activity;
    private int screen;
    private final static int SCREEN_PORTRAIT = 0;
    private final static int SCREEN_LANDSCAPE_LEFT = 90;
    private final static int SCREEN_LANDSCAPE_RIGHT = 270;

    private boolean isPushing;
    private SurfaceHolder holder;
    private VideoParam videoParam;
    private byte[] buffers;
    private byte[] raw;
    private Camera camera;
    private PushNative pushNative;

    public VideoPusher(Activity activity, SurfaceHolder holder, PushNative pushNative) {
        this.activity = activity;
        this.holder = holder;
        this.pushNative = pushNative;
        videoParam = new VideoParam(480, 320, Camera.CameraInfo.CAMERA_FACING_BACK);
        holder.addCallback(this);
    }

    public void switchCamera() {
        if (videoParam.getCameraId() == Camera.CameraInfo.CAMERA_FACING_BACK) {
            videoParam.setCameraId(Camera.CameraInfo.CAMERA_FACING_FRONT);
        } else {
            videoParam.setCameraId(Camera.CameraInfo.CAMERA_FACING_BACK);
        }
        //重新预览
        stopPreview();
        startPreview();
    }

    private void stopPreview() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    /**
     * 开始预览
     */
    private void startPreview() {
        try {
            //SurfaceView初始化完成，开始相机预览
            camera = Camera.open(videoParam.getCameraId());
            Camera.Parameters parameters = camera.getParameters();
            setPreviewSize(parameters);
            setPreviewOrientation(parameters);
            //设置相机参数  没用
            parameters.setPreviewFormat(ImageFormat.NV21); //YUV 预览图像的像素格式
            camera.setParameters(parameters);
            camera.setPreviewDisplay(holder);
            //获取预览图像数据
            int bitsPerPixel = ImageFormat.getBitsPerPixel(ImageFormat.NV21);
            buffers = new byte[videoParam.getWidth() * videoParam.getHeight() * bitsPerPixel / 8];
            raw = new byte[videoParam.getWidth() * videoParam.getHeight() * bitsPerPixel / 8];
            camera.addCallbackBuffer(buffers);
            camera.setPreviewCallbackWithBuffer(this);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setPreviewSize(Camera.Parameters parameters) {
        List<Integer> supportedPreviewFormats = parameters.getSupportedPreviewFormats();
        for (Integer integer : supportedPreviewFormats) {
            System.out.println("支持:" + integer);
        }
        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        Camera.Size size = supportedPreviewSizes.get(0);
        Log.d(TAG, "支持 " + size.width + "x" + size.height);
        int m = Math.abs(size.height * size.width - videoParam.getHeight() * videoParam.getWidth());
        supportedPreviewSizes.remove(0);
        Iterator<Camera.Size> iterator = supportedPreviewSizes.iterator();
        while (iterator.hasNext()) {
            Camera.Size next = iterator.next();
            Log.d(TAG, "支持 " + next.width + "x" + next.height);
            int n = Math.abs(next.height * next.width - videoParam.getHeight() * videoParam.getWidth());
            if (n < m) {
                m = n;
                size = next;
            }
        }
        videoParam.setHeight(size.height);
        videoParam.setWidth(size.width);
        parameters.setPreviewSize(videoParam.getWidth(), videoParam.getHeight());
        Log.d(TAG, "预览分辨率 width:" + size.width + " height:" + size.height);
    }

    private void setPreviewOrientation(Camera.Parameters parameters) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(videoParam.getCameraId(), info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        screen = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                screen = SCREEN_PORTRAIT;
                pushNative.setVideoOptions(videoParam.getHeight(), videoParam.getWidth(), videoParam.getBitrate(), videoParam.getFps());
                break;
            case Surface.ROTATION_90: // 横屏 左边是头部(home键在右边)
                screen = SCREEN_LANDSCAPE_LEFT;
                pushNative.setVideoOptions(videoParam.getWidth(), videoParam.getHeight(), videoParam.getBitrate(), videoParam.getFps());
                break;
            case Surface.ROTATION_180:
                screen = 180;
                break;
            case Surface.ROTATION_270:// 横屏 头部在右边
                screen = SCREEN_LANDSCAPE_RIGHT;
                pushNative.setVideoOptions(videoParam.getWidth(), videoParam.getHeight(), videoParam.getBitrate(), videoParam.getFps());
                break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + screen) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - screen + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (isPushing) {
            switch (screen) {
                case SCREEN_PORTRAIT:
                    portraitData2Raw(data);
                    break;
                case SCREEN_LANDSCAPE_LEFT:
                    raw = data;
                    break;
                case SCREEN_LANDSCAPE_RIGHT:
                    landscapeData2Raw(data);
                    break;
            }
            if (pushNative != null) {
                LogUtils.e("buffers.length="+buffers.length);
                pushNative.pushVideo(raw);
            }
        }
        camera.addCallbackBuffer(buffers);
    }

    private void landscapeData2Raw(byte[] data) {
        int width = videoParam.getWidth(), height = videoParam.getHeight();
        int y_len = width * height;
        int k = 0;
        // y数据倒叙插入raw中
        for (int i = y_len - 1; i > -1; i--) {
            raw[k] = data[i];
            k++;
        }
        // System.arraycopy(data, y_len, raw, y_len, uv_len);
        // v1 u1 v2 u2
        // v3 u3 v4 u4
        // 需要转换为:
        // v4 u4 v3 u3
        // v2 u2 v1 u1
        int maxpos = data.length - 1;
        int uv_len = y_len >> 2; // 4:1:1
        for (int i = 0; i < uv_len; i++) {
            int pos = i << 1;
            raw[y_len + i * 2] = data[maxpos - pos - 1];
            raw[y_len + i * 2 + 1] = data[maxpos - pos];
        }
    }

    private void portraitData2Raw(byte[] data) {
        int width = videoParam.getWidth(), height = videoParam.getHeight();
        int y_len = width * height;
        int uvHeight = height >> 1; // uv数据高为y数据高的一半
        int k = 0;
        if (videoParam.getCameraId() == Camera.CameraInfo.CAMERA_FACING_BACK) {
            for (int j = 0; j < width; j++) {
                for (int i = height - 1; i >= 0; i--) {
                    raw[k++] = data[width * i + j];
                }
            }
            for (int j = 0; j < width; j += 2) {
                for (int i = uvHeight - 1; i >= 0; i--) {
                    raw[k++] = data[y_len + width * i + j];
                    raw[k++] = data[y_len + width * i + j + 1];
                }
            }
        } else {
            for (int i = 0; i < width; i++) {
                int nPos = width - 1;
                for (int j = 0; j < height; j++) {
                    raw[k] = data[nPos - i];
                    k++;
                    nPos += width;
                }
            }
            for (int i = 0; i < width; i += 2) {
                int nPos = y_len + width - 1;
                for (int j = 0; j < uvHeight; j++) {
                    raw[k] = data[nPos - i - 1];
                    raw[k + 1] = data[nPos - i];
                    k += 2;
                    nPos += width;
                }
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        startPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void startPush() {
        isPushing = true;
    }

    @Override
    public void stopPush() {
        isPushing = false;
    }

    @Override
    public void release() {
        stopPreview();
    }
}
