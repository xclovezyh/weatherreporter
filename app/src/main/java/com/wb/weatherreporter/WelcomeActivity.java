package com.wb.weatherreporter;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

public class WelcomeActivity extends AppCompatActivity {

    private ImageView welcomeImg;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);

        if (Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
            //将状态栏设置为透明色
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        welcomeImg = (ImageView) findViewById(R.id.welcome_img);
        AlphaAnimation animation = new AlphaAnimation(0.3f,1.0f);
        animation.setDuration(3000);
        welcomeImg.startAnimation(animation);
        animation.setAnimationListener(new WelcomeActivity.AnimationImpl());
    }

    public class AnimationImpl implements Animation.AnimationListener{

        @Override
        public void onAnimationStart(Animation animation) {
            welcomeImg.setBackgroundResource(R.mipmap.welcome);
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            skip();
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

    private void skip() {
        Intent intent = new Intent(WelcomeActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }
}
