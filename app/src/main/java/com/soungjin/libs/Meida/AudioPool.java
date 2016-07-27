package com.soungjin.libs.Meida;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * http://duongame.blogspot.kr/2014/12/50-soundpool-audiopool.html
 * Created by 류 on 2016-04-16.
 */

public class AudioPool implements Runnable {
    private final static String TAG = "AudioPool";

    private static final AudioPool AUDIO_POOL = new AudioPool();

    int position = 0;
    public static int sampleRate = 22050;//고정 셈플레이트
    public static int channels = 2;
    public static int MAXBYTE = 22050;//스텍 최대 크기 5000
    public static int MOVEBYTE;//스텍 최대 크기 5000

    public boolean isStop = true;

    short buffer[];//작업용 버퍼
    short ViewBuffer[];//출력용 버퍼
    short tmp[];

    private static Thread thread;

    HashMap<Integer, String> mMediaRootMap = new HashMap<Integer, String>();
    HashMap<String, Integer> mMediaPointMap = new HashMap<String, Integer>();
    HashMap<Integer, OnMuiscEffect> mMediaEffectMap = new HashMap<Integer, OnMuiscEffect>();

    ArrayList<Stack> stacks = new ArrayList<>();//music Stack

    AudioTrack track;

    public interface OnCompletionListener{
        void onCompletion(int id);
    }

    public interface OnMuiscEffect{
        short[] onEffect(short arr[]);
    }

    public interface OnMuiscWave{
        void onEffect(byte arr[]);
    }

    OnCompletionListener onCompletionListener;
    OnMuiscWave onMuiscWave;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (onCompletionListener != null)
                onCompletionListener.onCompletion(msg.arg1);
            Log.d(TAG, "Finish Play :" + msg.arg1);
        }
    };

    public static AudioPool Create() {
        return  Create(44100, 2);
    }

    public static AudioPool Create(int SampleRate, int Channels) {
        if (Channels == 0)
            throw new NullPointerException("체널 잘못됨");
        channels = Channels;
        sampleRate = SampleRate;
        AUDIO_POOL.buffer = new short[MAXBYTE];
        AUDIO_POOL.ViewBuffer = new short[MAXBYTE];
        AUDIO_POOL.tmp = new short[MAXBYTE];
        return AUDIO_POOL;
    }

    public static AudioPool setBufferSize(int size){
        if (size >= 44100 || size < 5000)
            return AUDIO_POOL;
        MAXBYTE = size;
        AUDIO_POOL.buffer = new short[MAXBYTE];
        AUDIO_POOL.ViewBuffer = new short[MAXBYTE];
        AUDIO_POOL.tmp = new short[MAXBYTE];
        return AUDIO_POOL;
    }

    AudioPool() {
        sampleRate = 44100;
        channels = 2;
        MOVEBYTE = AudioTrack.getMinBufferSize (
                sampleRate ,
                channels == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT);
        track = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                sampleRate ,
                channels == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT,
                MOVEBYTE * 2,
                AudioTrack.MODE_STREAM);
        track.play();
        isStop = false;

        Log.d(TAG, "Create Track Sound :" + MAXBYTE);
    }

    public void ChangeSpeed (int Hz){
        if (Hz <= 0 || Hz > 48000 || Hz == sampleRate)
            return;
        track.stop();
        synchronized (track) {
            MOVEBYTE = AudioTrack.getMinBufferSize(
                    Hz,
                    channels == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT);
            track = new AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    Hz,
                    channels == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    MOVEBYTE * 2,
                    AudioTrack.MODE_STREAM);
            track.play();
        }
        sampleRate = Hz;
    }

    void MixBuffer(int position, @NonNull short buff[]){
        if (buff.length != MAXBYTE)
            return;
        if (mMediaEffectMap.containsKey(position)){
            OnMuiscEffect effect = mMediaEffectMap.get(position);
            if (effect != null)
                buff = effect.onEffect(buff);
        }
        if (buffer == null)
            buffer = new short[MAXBYTE];
        for (int i = 0; i < MAXBYTE; i++)
            if (buffer[i] == 0)
                buffer[i] = buff[i];
            else if (isEq(buff[i], buffer[i]))
                buffer[i] =  Max(buffer[i], buff[i]);
            else buffer[i] = Max(buffer[i], (short) ~buff[i]);
    }

    boolean isEq(short a, short b){
        if (a >= 0 || b >= 0)
            return true;
        else if (a < 0 || b < 0)
            return true;
        return false;
    }

    short Max(short a, short b){
        if (a >= b)
            return a;
        return b;
    }

    @Override
    public void run() {
        isStop = false;
        byte buffers[];
        long time;
        while (!isStop){
            buffers = new byte[MAXBYTE * 2];
            int size = stacks.size();
            int readSize = 0;
            time = System.currentTimeMillis();
            track.write(ViewBuffer, 0, ViewBuffer.length);
            for (int i = 0; i < size; i++){
                try {
                    Stack stack = stacks.get(i);
                    readSize = stack.stream.read(buffers);
                    if (readSize < 0){
                        Log.d(TAG, "Remove Sound :" + stack.position);
                        Message message = handler.obtainMessage();
                        message.arg1 = stack.position;
                        handler.sendMessage(message);
                        stack.stream.close();
                        stacks.remove(stack);
                        i--;
                        size = stacks.size();
                        continue;
                    }
                    stack.point += MAXBYTE *2;
                    MixBuffer(stack.position, getData(buffers));
                } catch (Exception e) {e.printStackTrace();}
            }
//            ViewBuffer = HanningWindow(buffer,0, buffer.length);
            if (buffer != null) {
                ViewBuffer = buffer.clone();
                if (onMuiscWave != null)
                    onMuiscWave.onEffect(get2Data(buffer));
            }
            buffer = null;
            Log.d("Time", "LOG" + (System.currentTimeMillis() - time));
        }
    }

    public int load(String dir){
        if (mMediaPointMap.containsKey(dir)){
            return mMediaPointMap.get(dir);
        }
        position ++;
        mMediaRootMap.put(position, dir);
        mMediaPointMap.put(dir,position);
        Log.d(TAG, "Sound load :" + dir + "/" + position);
        return position;
    }

    public void play(int id) throws IOException {
        boolean isPlay = false;
        int p = 0;
        if (thread == null || isStop){
            thread = new Thread(this);
            thread.start();
        }
        if (track == null)
            ChangeSpeed(sampleRate);
        synchronized (stacks){
            for (Stack stack : stacks)
                if (stack.position == id)
                    isPlay = true;
            Stack stack;
            if (isPlay)
                stack = stacks.get(p);
            else {
                stack = new Stack();
                File f = new File(mMediaRootMap.get(id));
                stack.stream = new BufferedInputStream(new FileInputStream(f));
                stack.stream.mark(id);
                stack.size = (int) f.length();
            }
            stack.point = 0;
            stack.position = id;
            if (isPlay)
                stack.stream.reset();
            else
                stacks.add(stack);
            Log.d(TAG, "Sound play :" + id);
        }
    }

    public int getSize(){//재생중인 음원의 크기
        return stacks.size();
    }

    public void clear(){//모든 스텍과, 데이터를 제거
        mMediaRootMap.clear();
        mMediaPointMap.clear();
        stacks.clear();
        stop();
    }

    public void pause(){
        isStop = true;
        try {
            thread.join();
        } catch (Exception e) {}
    }

    public void stop(){
        isStop = true;
        try {
            track.release();
            thread.join();
        } catch (Exception e) {}
        stacks.clear();
    }

    public void stop(int position) throws IOException {
        int size = stacks.size();
        for (int i = 0; i < size; i++){
            Stack stack = stacks.get(i);
            if (stack.position == position){
                stack.stream.close();
                stacks.remove(position);
                size--;
            }
        }
    }

    public int getTime(int position) {
        Stack stack = null;
        for (int i = 0; i < stacks.size(); i++)
            if (stacks.get(i).position == position)
                stack = stacks.get(i);
        if (stack != null){
            return stack.point;
        }else return -1;
    }

    public int getMaxTime(int position) {
        Stack stack = null;
        for (int i = 0; i < stacks.size(); i++)
            if (stacks.get(i).position == position)
                stack = stacks.get(i);
        if (stack != null){
            return stack.size;
        }else return -1;
    }

    public void setOnCompletionListener(OnCompletionListener onCompletionListener) {
        this.onCompletionListener = onCompletionListener;
    }

    public void setOnMuiscEffect(int position, OnMuiscEffect onMuiscEffect) {
        if (!mMediaEffectMap.containsKey(position)){
            mMediaEffectMap.put(position, onMuiscEffect);
        }else{
            mMediaEffectMap.remove(position);
            mMediaEffectMap.put(position, onMuiscEffect);
        }
    }

    public void setOnMuiscWave(OnMuiscWave onMuiscWave) {
        this.onMuiscWave = onMuiscWave;
    }

    static short[] getData(byte arr[]){
        short arrs[] = new short[arr.length/2];
        for (int i = 0; i < arr.length; i+=2)
            arrs[i/2] = get2Data(arr, i, i+2);
        return arrs;
    }

    static short get2Data(byte arr[], int start, int end){
        byte data[] = new byte[end - start];
        for (int i = 0; i < data.length; i++)
            data[i] = arr[start + i];
        return get2Data(data);
    }

    static short get2Data(byte arr[]){
        return (short) (((0xff & arr[1]) << 8) |
                ((0xff & arr[0])));
    }

//    public static byte[] getSubData(byte[]arr, int subper){
//        byte out[] = new byte[subper/100];
//        int sampel = (int) (arr.length * (0.01 * subper));
//        for (int i = 0; i < out.length; i++)
//            out[i] = arr[i/sampel];
//        return out;
//    }

    public static byte[] get2Data(short[] arr){
        byte out[] = new byte[arr.length*2];
        for (int i = 0; i <out.length; i+=2){
            byte b[] = get2Data(arr[i/2]);
            out[i] = b[0];
            out[i+1] = b[1];
        }
        return out;
    }

    public static byte[] get2Data(short data){
        return new byte[]{
                (byte) (data & 0xff),
                (byte) ((data >> 8) & 0xff)};
    }

    class Stack{
        BufferedInputStream stream;
        int position;
        int size;
        int point;
    }
}

