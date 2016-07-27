package com.soungjin.libs.UI;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.soungjin.libs.R;

/**
 * Created by RyuSoungJin on 2016-07-04.
 */
public class Pad extends LinearLayout implements View.OnTouchListener{

    public static final Integer PadColor[] = {
            0,  Color.rgb(244, 251, 255),   Color.rgb(247, 255, 255),   Color.rgb(251, 251, 249),   Color.rgb(253, 167, 196),   Color.rgb(233, 23, 36), Color.rgb(224, 16, 38), Color.rgb(137, 2, 16),  Color.rgb(224, 220, 211),   //1 ~ 8
            Color.rgb(230, 102, 5),     Color.rgb(167, 47, 23),     Color.rgb(142, 48, 10),     Color.rgb(238, 226, 140),   Color.rgb(164, 169, 5), Color.rgb(116, 149, 0), Color.rgb(110, 142, 0), Color.rgb(128, 230, 120),   // 9 ~ 16
            Color.rgb(80, 247, 35),     Color.rgb(51, 238, 23),     Color.rgb(9, 115, 5),       Color.rgb(113, 241, 190),   Color.rgb(0, 195, 16),  Color.rgb(6, 184, 26),  Color.rgb(0, 85, 14),   Color.rgb(126, 239, 196),   // 16 ~ 24
            Color.rgb(16, 218, 103),    Color.rgb(0, 213, 78),      Color.rgb(7, 119, 17),      Color.rgb(167, 253, 224),   Color.rgb(44, 253, 191), Color.rgb(0, 165, 103), Color.rgb(8, 141, 112), Color.rgb(171, 247, 243),   // 25~ 32
            Color.rgb(86,228,230),      Color.rgb(79,226,234),      Color.rgb(5,185,173),       Color.rgb(196,248,255),     Color.rgb(138,242,251), Color.rgb(44,182,244),  Color.rgb(1,122,205),   Color.rgb(119,197,245),     // 33 ~ 40
            Color.rgb(120,223,255),     Color.rgb(64,178,249),      Color.rgb(7,94,210),        Color.rgb(94,146,245),      Color.rgb(62,148,255),  Color.rgb(19,91,235),   Color.rgb(0,88,217),    Color.rgb(194,214,247),     // 41 ~ 48
            Color.rgb(125,184,255),     Color.rgb(72,109,241),      Color.rgb(16,54,177),       Color.rgb(217,198,243),     Color.rgb(180,105,247), Color.rgb(209,169,239), Color.rgb(113,66,172),  Color.rgb(236,191,224),     // 49 ~ 56
            Color.rgb(238,87,203),      Color.rgb(199,49,157),      Color.rgb(161,20,112),      Color.rgb(202,24,24),       Color.rgb(164,73,0),    Color.rgb(176,180,7),   Color.rgb(66,136,38),   Color.rgb(1,148,16),        // 57 ~ 64

            Color.rgb(0,189,136),       Color.rgb(5,104,234),       Color.rgb(51,132,255),      Color.rgb(0,138,209),       Color.rgb(52,99,249),   Color.rgb(169,198,228), Color.rgb(64,93,127),   Color.rgb(211,17,26),       // 65 ~ 72
            Color.rgb(160,235,90),      Color.rgb(154,216,11),      Color.rgb(96,228,64),       Color.rgb(0,196,23),        Color.rgb(80,225,222),  Color.rgb(73,205,255),  Color.rgb(36,138,255),  Color.rgb(105,153,251),     // 73 ~ 80
            Color.rgb(136,133,255),     Color.rgb(193,141,239),     Color.rgb(76,54,15),        Color.rgb(226,121,0),       Color.rgb(230,125,0),   Color.rgb(156,241,16),  Color.rgb(14,255,118),  Color.rgb(95,250,146),      // 81 ~ 88
            Color.rgb(116,243,216),     Color.rgb(165,243,255),     Color.rgb(112,188,250),     Color.rgb(95,175,248),      Color.rgb(130,173,255), Color.rgb(188,158,255), Color.rgb(204,81,205),  Color.rgb(196,121,0),       // 89 ~ 96
            Color.rgb(182,195,4),       Color.rgb(130,251,22),      Color.rgb(140,155,10),      Color.rgb(110,93,15),       Color.rgb(0,185,73),    Color.rgb(0,178,146),   Color.rgb(27,98,206),   Color.rgb(45,116,224),      // 97 ~ 104
            Color.rgb(154,120,95),      Color.rgb(194,0,27),        Color.rgb(189,126,111),     Color.rgb(237,175,98),      Color.rgb(222,230,108), Color.rgb(166,237,117), Color.rgb(99,225,30),   Color.rgb(38,72,179),       // 105 ~ 112
            Color.rgb(198,223,202),     Color.rgb(132,228,227),     Color.rgb(150,193,255),     Color.rgb(170,201,232),     Color.rgb(103,131,171), Color.rgb(117,149,200), Color.rgb(169,208,237), Color.rgb(170,14,18),       // 113 ~ 120
            Color.rgb(107,7,5),         Color.rgb(59,249,123),      Color.rgb(6,130,20),        Color.rgb(160,187,0),       Color.rgb(111,122,47),  Color.rgb(187,137,24),  Color.rgb(100,36,11)                                // 121 ~ 127
    };

    public static final String TAG = "Pad Layout";
    public static final int PAD_LAY_SIZE = 8;

    public static final int COLOR_BACKGROUND = Color.rgb(0x40,0x38,0x48);
    public static final int COLOR_PADBUTTON = Color.rgb(0xbe,0xc6,0xd1);


    Context context;

    int length;
    public int x, y;
    int bluck_Size = 0;
    int s_size;

    RelativeLayout[][] buttons;
    LinearLayout row[];
    public interface OnPadTouchListener{
        void onPadTouchListener(View v, int x, int y, boolean isDown);
    };

    OnPadTouchListener onPadTouchListener;


    public Pad(Context context) {
        super(context);
        this.context = context;
    }

    public Pad(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public Pad(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Pad(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, heightMeasureSpec);
        length = heightMeasureSpec;
//        Log.d(TAG, "Width :" +widthMeasureSpec + "/Height :" + heightMeasureSpec + "\tCreate");
    }

    public int init(){
        return this.init(PAD_LAY_SIZE, PAD_LAY_SIZE);
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int init(int x, int y){
        Log.d(TAG, "Call" + bluck_Size + "/" + length);
        if (bluck_Size != 0)
            return length;
        Log.d(TAG, "?" + x + "/" +  y);
        setOrientation(VERTICAL);
        setBackgroundColor(COLOR_BACKGROUND);
        this.x = x;
        this.y = y;
        row = new LinearLayout[x];
        buttons = new RelativeLayout[x][y];
        bluck_Size = length/ Math.max(x,y);
        s_size = bluck_Size/2;

        LayoutParams params = new LayoutParams(length, bluck_Size);
        params.weight = 1;
        for (int i = x - 1; i >= 0; i--){
            row[i] = new LinearLayout(context);
            row[i].setOrientation(HORIZONTAL);
            addView(row[i], params);
            for (int j = 0; j < y; j++){
                buttons[i][j] = (RelativeLayout) View.inflate(context,R.layout.pad_button, null);
                buttons[i][j].setOnTouchListener(this);
                buttons[i][j].setId((i * 10) + j);
                row[i].addView(buttons[i][j], bluck_Size, bluck_Size);
                buttons[i][j].findViewById(R.id.bt).setBackground(GetDrawableO(R.drawable.pad_button_base, COLOR_PADBUTTON));
            }
        }
        refreshDrawableState();
        Log.d(TAG, "출력 완료");
        return length;
    }

    public Drawable GetDrawableO(int drawableResId, int color) {
        Drawable drawable =  getResources().getDrawable(drawableResId);
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        return drawable;
    }

    public void setOnPadTouchListener(OnPadTouchListener onPadTouchListener) {
        this.onPadTouchListener = onPadTouchListener;
    }

    public RelativeLayout[][] getButton() {
        return buttons;
    }

    public RelativeLayout getButton(int x, int y) {
        return buttons[x][y];
    }

    public void setButtonColor(int x, int y, int colorcode){
        if (colorcode >= PadColor.length)
            return;
        setColor(x, y, colorcode);
    }

    void setColor(int x, int y, int color){
        Message msg = handler.obtainMessage();
        msg.arg1 = x;
        msg.arg2 = y;
        msg.obj = color;
        handler.sendMessage(msg);
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int x = msg.arg1;
            int y = msg.arg2;
            int colorcode = (int) msg.obj;
            if (colorcode == 0)
                buttons[x][y].findViewById(R.id.bt).setBackground(GetDrawableO(R.drawable.pad_button_base, COLOR_PADBUTTON));
            else
                buttons[x][y].findViewById(R.id.bt).setBackground(GetDrawableO(R.drawable.pad_button_base, PadColor[colorcode]));
        }
    };

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int id = v.getId();
        try {
            if (event.getAction() != MotionEvent.ACTION_MOVE)
                onPadTouchListener.onPadTouchListener(v, id / 10, id % 10, event.getAction()!= MotionEvent.ACTION_UP);
        }catch (Exception e){;}
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d("Pad", "Drawing Px" + canvas.getWidth() + "/" + canvas.getHeight());
    }
}