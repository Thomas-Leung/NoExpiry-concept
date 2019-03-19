package thomas.com.NoExpiry;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class SplashActivity extends AppCompatActivity {

    private ImageView logo, splash_l, splash_r;

    Animation animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // animation
        logo = findViewById(R.id.logo);

        animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.push_down);
        logo.setAnimation(animation);

        // implement splash screen
        Thread thread = new Thread(){
            @Override
            public void run() {
                try {
                    sleep(1410); // sleep for 4s
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                super.run();
            }
        };
        thread.start();
    }
}

