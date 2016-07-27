package com.soungjin.libs.UI;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;

/**
 * Created by RyuSoungJin on 2016-07-10.
 */

public class LEDEvent extends Point {

    public static final String HEAD = "LEDE";

    private LEDData led[];
    public int width, hight;
    private boolean isDownEvent = true;


    public LEDEvent(byte[] buffer) {
        super(getByteToLingth(buffer, 4, BUFFER_SIZE));
        if (!isFile(buffer))
            getError();
        int readPoint = 4 + BUFFER_SIZE;
        led = new LEDData[1];
        width = buffer[readPoint++];
        hight = buffer[readPoint++];
        isDownEvent = buffer[readPoint++]==0;
        int bsize = buffer[readPoint++];
        if (bsize == 0)
            return;
        BigInteger size = new BigInteger(getByteToLingth(buffer, readPoint, bsize));
        readPoint+= bsize;
        setLEDData(getByteToLingth(buffer, readPoint, Integer.parseInt(size.toString())));
    }

    void setLEDData(byte[] buffer){
        int readPoint = 0;
        int bufferSize = LEDData.getBufferSize(width, hight);
        int timeDataSize = buffer[bufferSize];
        int AllDataSize = bufferSize + timeDataSize + 1;
        byte[] tmp = new byte[AllDataSize];
        do{
            System.arraycopy(buffer, readPoint, tmp, 0, AllDataSize);
            LEDData data = new LEDData(tmp);
            add(data.time, data);
            readPoint += AllDataSize;
            if (readPoint >= buffer.length)
                break;
            timeDataSize = buffer[readPoint + bufferSize];
            AllDataSize = bufferSize + timeDataSize + 1;
            tmp = null;
            tmp = new byte[AllDataSize];
        }while (true);
    }

    public LEDEvent(int width, int hight, int x, int y) {
        this(width, hight, 1, x, y);
    }

    public LEDEvent(int width, int hight, int count, int x, int y) {
        super(x, y);
        led = new LEDData[count];
        this.width = width;
        this.hight = hight;
    }

    /////////////////////////////////////////////////////////////////////////////

    public LED get(int count){
        return led[count];
    }

    public int getTime(int count){
        if (led[count] == null)
            return 0;
        return led[count].time;
    }

    public void setTime(int count, int time){
        if (led[count] == null)
            led[count] = new LEDData(width,hight, time, x,y);
        else led[count].time = time;
    }

    public int reArrayToNull(){
        int size = led.length, count = 0;
        for (int i = 0; i < size -1; i++){
            if (led[i] == null){
                remove(i);
                size--;
                count ++;
            }
        }
        return count;
    }

    public int size(){
        return led.length;
    }

    /////////////////////////////////////////////////////////////////////////////

    public boolean isDownEvent(){
        return isDownEvent;
    }

    public void setIsDown(boolean b){
        this.isDownEvent = b;
    }

    /////////////////////////////////////////////////////////////////////////////


    public void set(int index, LED led){
        if (index > 255)
            return;
        if (index >= this.led.length)return;
        set(index, led.arr);
    }

    public void set(int index, int time, LED led){
        if (index > 255)
            return;
        if (index >= this.led.length)return;
        set(index, time, led.arr);
    }

    public void set(int index, int arr[][]){
        if (index > 255)
            return;
        if (index >= led.length)return;
        if (led[index] == null)
            led[index] = new LEDData(width, hight, 0, x, y);
        led[index].set(arr);
    }

    public void set(int index, int time, int arr[][]){
        if (index > 255)
            return;
        if (index >= led.length)return;
        set(index, arr);
        led[index].time = time;
    }

    public void set(int index, int x, int y, int color){
        if (index > 255)
            return;
        if(led[index] == null)
            return;
        led[index].get()[x][y] = color;
    }

    public void set(int index, int color){
        if (index > 255)
            return;
        if(led[index] == null)
            led[index] = new LEDData(width, hight, 0, x, y);
        int arr[][] = led[index].get();
        for (int i = 0; i < width; i++)
            for (int j = 0; j < hight; j++)
                arr[x][y] = color;
    }


    /////////////////////////////////////////////////////////////////////////////

    boolean isLast(int index){
        if (index == led.length -1)
            return true;
        for (int i = index; i < led.length; i++)
            if (led[i] != null)
                return false;
        return true;
    }

    int add(){
        int i;
        for (i = 0; i < led.length; i++)
            if (led[i] == null)
                if (isLast(i))
                    break;
        //i = last item point;
        if (i == this.led.length){
            if (!expand(1))
                return -1;
        }
        return i;
    }

    public void add(int time, LED led){
        int i = add();
        if (i == -1)return;
        this.led[i] = new LEDData(time, led);
    }

    public void add(int time, int arr[][]){
        int i = add();
        if (i == -1)return;
        this.led[i] = new LEDData(this, time, arr);
    }

    /////////////////////////////////////////////////////////////////////////////

    public void remove(int index){
        if (index >= led.length)return;
        push(index, index+1);
        narrow(1);
    }

    /////////////////////////////////////////////////////////////////////////////

    /**
     * 배열 확장
     * @param i
     * @return
     */
    private boolean expand(int i) {
        int size = led.length;
        if (size > 255)
            return false;
        LEDData tmp[] = new  LEDData[size + i];
        System.arraycopy(led, 0, tmp, 0, size);
        led = tmp;
        return true;
    }

    /**
     * 배열 축소
     * @param i
     * @return
     */
    private boolean narrow(int i){
        int size = led.length - i;
        if (size < 0)
            return false;
        LEDData tmp[] = new  LEDData[size];
        System.arraycopy(led, 0, tmp, 0, size);
        led = tmp;
        return true;
    }

    /**
     * 배열 옮기기
     * @param sp	시작위치(채우기 시작)
     * @param ep	끝 위치
     * @return
     */
    private boolean push(int sp,int ep){
        int space = led.length - ep;
        if (space < 0)return false;
        if (space == 0){
            for (int i = sp; i < led.length; i++)
                led[i] = null;
            return true;
        }
        //space > 0
        else{
            LEDData tmp[] = new LEDData[space];
            System.arraycopy(led, ep, tmp, 0, space);
            System.arraycopy(tmp, 0, led, sp, space);
            for (int i = sp + space; i < led.length; i++)
                led[i] = null;
            tmp = null;
        }
        return true;
    }

    /////////////////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Size:" + led.length);
        buffer.append('\n');
        buffer.append("다운이벤트:");
        buffer.append(isDownEvent);
        buffer.append('\n');
        for (int i = 0; i < led.length; i++){
            if (led[i] == null)
                continue;
            buffer.append('\n');
            buffer.append(i);
            buffer.append("point\n");
            buffer.append(led[i].toString());
        }
        return buffer.toString();
    }

    @Override
    public Object clone() {
        LEDEvent event = new LEDEvent(width, hight, led.length, x, y);
        event.led = led.clone();
        return event;
    }

    @Override
    public byte[] toByte() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        write(stream, HEAD.getBytes());
        byte[] buffer = super.toByte();
        write(stream, buffer);
        stream.write(width);
        stream.write(hight);
        stream.write(isDownEvent?0:1);
        buffer = getDatas();
        if (buffer.length == 0) {
            stream.write(0);
            return stream.toByteArray();
        }
        byte[] size = BigInteger.valueOf(buffer.length).toByteArray();
        stream.write(size.length);
        write(stream, size);
        write(stream, buffer);
        return stream.toByteArray();
    }


    /////////////////////////////////////////////////////////////////////////////

    byte[] getDatas(){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        for (int i = 0; i < led.length; i++)
            if (led[i] == null)
                continue;
            else
                write(stream, led[i].toByte());
        return stream.toByteArray();
    }

    public static boolean isFile(byte[] buffer){
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++)
            b[i] = buffer[i];
        if (!HEAD.equals(toByteString(b)))
            return false;
        return true;
    }

    /////////////////////////////////////////////////////////////////////////////

    class LEDData extends LED{

        int time;

        public LEDData(byte[] buffer) {
            super(buffer);
            int readPoint  = getBufferSize(width,hight);
            int size = buffer[readPoint++];
            time = Integer.parseInt(new BigInteger(getByteToLingth(buffer, readPoint, size)).toString());
        }

        public LEDData(int width, int hight, int time, int x, int y) {
            super(width, hight, x, y);
            this.time = time;
        }

        public LEDData(int time,LED led) {
            super(led.width, led.hight, led.x, led.y);
            arr = led.arr.clone();
            this.time = time;
        }
        public LEDData(LED led) {
            super(led.width, led.hight, led.x, led.y);
            arr = led.arr.clone();
            this.time = 1500;
        }

        public LEDData(Point point,int time, int arr[][]) {
            super(arr.length, arr[0].length, point.x, point.y);
            this.time = time;
            this.arr = arr.clone();
        }

        @Override
        public String toString() {
            return time + super.toString();
        }

        @Override
        public Object clone() throws CloneNotSupportedException {
            return new LEDData(time, (LED) super.clone());
        }

        @Override
        public byte[] toByte() {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            write(stream, super.toByte());
            byte[] buffer = BigInteger.valueOf(time).toByteArray();
            stream.write(buffer.length);
            write(stream,buffer);
            return stream.toByteArray();
        }
    }
}
