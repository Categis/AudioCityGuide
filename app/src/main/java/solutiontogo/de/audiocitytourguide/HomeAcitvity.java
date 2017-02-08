package solutiontogo.de.audiocitytourguide;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by shivaramak on 02/02/2017.
 */

public class HomeAcitvity extends AppCompatActivity {

    ImageView ivHome;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        ivHome = (ImageView) findViewById(R.id.ivHome);
        ivHome.setScaleType(ImageView.ScaleType.FIT_XY);
        ivHome.setImageResource(R.drawable.wikivoice_home_screen);

        ivHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(HomeAcitvity.this, NavigationHeader.class);
                startActivity(mainIntent);
            }
        });

    }
}
