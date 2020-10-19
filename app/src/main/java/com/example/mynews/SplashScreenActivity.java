package com.example.mynews;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashScreenActivity extends AppCompatActivity {

    //CREATE THE VIEW FOR TEXT AND IMAGES
    Animation topAnim, bottomAnim;
    ImageView splashImageView;
    TextView titleTextview, sloganTextview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.down_animation);

        //GET ALL THE VIEWS
        splashImageView = findViewById(R.id.splash_screen_imageView);
        titleTextview = findViewById(R.id.splash_screen_title_textview);
        sloganTextview = findViewById(R.id.splash_screen_slogan_textview);

        //SET ANIMATION ON THE VIEWS
        splashImageView.setAnimation(topAnim);
        titleTextview.setAnimation(bottomAnim);
        sloganTextview.setAnimation(bottomAnim);

        //HANDLER TO RUN ANIMATION FOR 3s ONLY
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 4000);
    }
}