package com.levylin.study.ffmpeg.live;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.levylin.study.ffmpeg.R;

public class ZhuboActivity extends AppCompatActivity {

    private static final String URL = "rtmp://47.94.192.215/myapp/mystream";

    private LivePusher livePusher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zhubo);
        SurfaceView surfaceView = findViewById(R.id.surfaceView);
        livePusher = new LivePusher(this);
        livePusher.prepare(surfaceView.getHolder());

        final Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (livePusher.isPushing()) {
                    livePusher.stopPusher();
                    button.setText("推流");
                } else {
                    livePusher.startPusher(URL);
                    button.setText("暂停");
                }
            }
        });
    }

    public void start_push(View view) {
        livePusher.startPusher(URL);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        livePusher.relase();
    }

    public void switch_camera(View view) {
        livePusher.switchCamera();
    }
}
