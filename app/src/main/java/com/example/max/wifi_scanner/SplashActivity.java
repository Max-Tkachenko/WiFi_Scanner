package com.example.max.wifi_scanner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Window;
import android.view.WindowManager;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.splash_layout);

        final Intent intent = new Intent(this, MainActivity.class);

        Thread splash_time = new Thread() {

            public void run() {
                try {
                    int SplashTimer = 0;
                    while (SplashTimer < 4000) {
                        sleep(100);
                        SplashTimer = SplashTimer + 100;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    finish();
                    startActivity(intent);
                }
            }
        };
        splash_time.start();
    }
}
