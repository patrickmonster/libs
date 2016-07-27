package com.soungjin.libs.Meida.Lunchpad;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.WindowManager;


/**
 * Created by 류 on 2016-04-10.
 */
public abstract class BaseActivity extends Activity {
    public static int width = 0, hight = 0;

    public static char slash = System.getProperty("file.separator", "/").charAt(0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Display display = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        width = display.getWidth();
        hight = display.getHeight();
    }

    public int getHight() {
        return hight;
    }

    public int getWidth() {
        return width;
    }
}
