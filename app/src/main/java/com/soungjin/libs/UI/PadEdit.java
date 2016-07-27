package com.soungjin.libs.UI;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.soungjin.libs.R;

/**
 * Created by RyuSoungJin on 2016-07-20.
 */
public class PadEdit extends Pad implements Pad.OnPadTouchListener {

    private LED led;
    private static boolean isTouchCancle = true;
    private static int color = 0;
    int select = 0;

    OnPadTouch touch;

    public interface OnPadTouch{
        void setChangeData(int position, int x, int y, int color);
    }

    public PadEdit(Context context) {
        super(context);
    }

    public PadEdit(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PadEdit(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PadEdit(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setSelect(int select) {
        this.select = select;
    }

    public void SetColorCode(LED led){
        try {
            if (led == null){
                led = new LED(8,8,0,0);
            }else
                this.led = (LED) led.clone();
        } catch (CloneNotSupportedException e) {
        }
    }

    public LED getLED(){
        return led;
    }

    public static void setColor(int colorCode){
        color = colorCode;
    }

    @Override
    public int init(int x, int y) {
        int length = super.init(x,y);
        setOnPadTouchListener(this);
        int arr[][];
        if (led != null) {
            arr = led.get();
            for (int i = 0; i < arr.length; i++)
                for (int j = 0; j < arr[i].length; j++)
                    if (arr[i][j] != (-1 | 0))
                        setButtonColor(i, j, arr[i][j]);
        }else {
            arr = new int[x][y];
            for (int i = 0; i < arr.length; i++)
                for (int j = 0; j < arr[i].length; j++)
                    arr[i][j] = 0;
        }
        return length;
    }

    @Override
    public void onPadTouchListener(View v, int x, int y, boolean isDown) {
        if (isTouchCancle)
            return;
        if (!isDown){
            if (color == 0)
                v.findViewById(R.id.bt).setBackground(GetDrawableO(R.drawable.pad_button_base, COLOR_PADBUTTON));
            else
                v.findViewById(R.id.bt).setBackground(GetDrawableO(R.drawable.pad_button_base, PadColor[color]));
            if (touch != null){
                touch.setChangeData(select, x,y, color);
            }
            led.set(x,y, color);
        }
    }

    public Drawable GetDrawableO(int drawableResId, int color) {
        Drawable drawable =  getResources().getDrawable(drawableResId);
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        return drawable;
    }

    public void setOnPadTouch(OnPadTouch onPadTouch) {
        this.touch = onPadTouch;
    }

    public static void tc(boolean isTouch){
        isTouchCancle = isTouch;
    }
}
