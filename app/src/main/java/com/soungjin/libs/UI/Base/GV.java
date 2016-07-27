package com.soungjin.libs.UI.Base;

import android.app.Activity;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.View;

/**
 * Created by rsj11 on 2015-10-14.
 */
public abstract class GV extends Activity {

    public GB g;
    public abstract void onDraws(Canvas canvas);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        g = new GB(this) {
            @Override
            public void Run(Canvas canvas) {
                onDraws(canvas);
            }
        };
        setContentView(g);
    }

    @Override
    protected void onStop() {
        super.onStop();
        g.Stop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        g.Pause(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        g.Restart();
    }

    public void setOnTouch(View.OnTouchListener touch){
        g.setOnTouchListener(touch);
    }

}
