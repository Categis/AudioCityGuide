package solutiontogo.de.audiocitytourguide.splash;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.WindowManager;
import android.widget.ImageView;

import java.util.Random;

import solutiontogo.de.audiocitytourguide.HomeAcitvity;
import solutiontogo.de.audiocitytourguide.R;

/**
 * Created by shivaramak on 02/02/2017.
 */

public class SplashScreen extends Activity {

    Thread splashTread;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryText));
        }
        imageView = (ImageView) findViewById(R.id.ivSplashscreen);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);

        int[] ids = new int[]{R.drawable.speaking_world_splash_screen, R.drawable.speaking_world_splash_screen, R.drawable.speaking_world_splash_screen};

        Random randomGenerator = new Random();

        int r = randomGenerator.nextInt(ids.length);

        this.imageView.setImageDrawable(getResources().getDrawable(ids[r]));

        Thread timer = new Thread() {
            public void run() {
                try {
                    sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    Intent intent = new Intent(SplashScreen.this,
                            HomeAcitvity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    //overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    SplashScreen.this.finish();
                }
            }
        };
        timer.start();
    }

}
