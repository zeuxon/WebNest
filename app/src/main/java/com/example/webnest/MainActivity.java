package com.example.webnest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView welcomeTitle = findViewById(R.id.welcomeTitle);
        TextView welcomeMessage = findViewById(R.id.welcomeMessage);
        Button loginButton = findViewById(R.id.goToLoginButton);

        Animation welcomeAnimation = AnimationUtils.loadAnimation(this, R.anim.welcome_animation);
        Animation buttonScaleAnimation = AnimationUtils.loadAnimation(this, R.anim.button_scale_animation);

        welcomeTitle.startAnimation(welcomeAnimation);
        welcomeMessage.startAnimation(welcomeAnimation);

        welcomeAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                loginButton.startAnimation(buttonScaleAnimation);
                loginButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
    }

    public void goToLogin(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}