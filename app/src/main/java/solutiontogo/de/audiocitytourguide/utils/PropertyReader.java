package solutiontogo.de.audiocitytourguide.utils;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by shivaramak on 12/01/2017.
 */

public class PropertyReader {
    public static Properties properties = null;
    InputStream inputStream = null;
    public PropertyReader(Context context) {
        try {
            if(properties == null){
                properties = new Properties();
                inputStream = context.getAssets().open("app.properties");
                properties.load(inputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
