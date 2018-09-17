package br.com.esc.meuslembretes;

import android.content.Intent;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;


public class SplashScreenActivity extends AppCompatActivity {

    private Handler handler = new SplashHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash_screen);

        getSupportActionBar().hide();

        handler.sendMessageDelayed(new Message(), 1200);
    }

    private class SplashHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
