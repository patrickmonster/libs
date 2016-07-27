package com.soungjin.libs.UI.Base;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

/**
 * Created by 류 on 2016-05-15.
 */
public abstract class GB extends SurfaceView implements Runnable, SurfaceHolder.Callback{

    public abstract void Run(Canvas canvas);

    private SurfaceHolder holder;
    private Thread thread;
    public Context context;

    private boolean isStop = false;//안전한 종료를 위한...
    private boolean isWait = false;

    public Paint TexterOptions = new Paint();

    public int Width = -1, Height = -1;
    private int px = 0;

    public int Frame = 0;
    public static int FreameX = 50, FrameY = 50;

    private Handler handler;

    private Canvas canvas = null;

    public GB(Context context) {
        super(context);
        this.context = context;
        holder = getHolder();
        holder.addCallback(this);
        thread = new Thread(this);
        setFocusable(true);
        init();
    }

    public void setFrameWrite(){
        if (handler != null)
            return;
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                Frame = px;
                px = 0;
                handler.sendEmptyMessageDelayed(0,1000);
            }
        };
        handler.sendEmptyMessage(0);
    }

    public void removeFrameWrite(){
        Frame = 0;
        handler.removeMessages(0);
        handler = null;
    }



    void init(){
        Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Width = display.getWidth();
        Height = display.getHeight();
        Log.d("Canvas", "W :" + Width + "\tH :" + Height + "Create!");
        TexterOptions.setAntiAlias(true);
        TexterOptions.setColor(Color.BLACK);
        TexterOptions.setTextSize(50);
    }

    @Override
    public void run() {
        Log.d("Canvas", "Run...");

        while (!isStop){
            canvas = holder.lockCanvas();
            try {
                synchronized (holder) {
                    if (px >= 1000)
                        px = 0;
                    px++;
                    Run(canvas);
                    if (handler != null)
                        canvas.drawText(Frame + "FPS", FreameX, FrameY, TexterOptions);
                }
            }catch (Exception e){
            }finally {
                if (canvas != null)
                    holder.unlockCanvasAndPost(canvas);
            }

            synchronized (thread){
                if (isWait){
                    try{
                        thread.wait();
                    }catch (Exception e){ }
                }
            }
        }
    }

    public void Stop(){
        isStop = true;
        if (handler != null)
            handler.removeMessages(0);
        synchronized (thread){
            thread.notify();
        }
    }

    public void Pause(boolean wait){
        isWait = wait;
        synchronized (thread){
            thread.notify();

        }
    }

    public void Restart(){
        Stop();
        thread = null;
        isWait = false;
        isStop = false;
        thread = new Thread(this);
        thread.start();
    }

    public Bitmap getResource(int resource){
        return BitmapFactory.decodeResource(getResources(), resource);
    }

    public Bitmap getResource(int resource, int width, int height){
        return Bitmap.createScaledBitmap(getResource(resource), width, height, false);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            thread.start();
        }catch (Exception e){
            Restart();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Width = width;
        Height = height;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Stop();
    }

    public static class Draw{//비트맵 생성
        public Canvas canvas;
        public Bitmap bitmap;

        public Draw(Bitmap bitmap) {
            this.bitmap = bitmap;
            this.canvas = new Canvas(bitmap);
        }

        public Draw(int width, int height) {
            this.bitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
            this.canvas = new Canvas(this.bitmap);
        }

        public Canvas getCanvas() {
            return this.canvas;
        }

        public Bitmap getBitmap() {
            return this.bitmap;
        }
    }
}
