package com.soungjin.libs.UI;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

/**
 * Created by RyuSoungJin on 2016-07-10.
 */
public class LED extends Point{

    public static final String HEAD = "LED ";


    int width, hight;
    int arr[][];

    public LED(byte[] buffer) {
        super(getByteToLingth(buffer, 4, BUFFER_SIZE));
        if (!isFile(buffer))
            getError();
        int readPoint = 4 + BUFFER_SIZE;
        width = buffer[readPoint++];
        hight = buffer[readPoint++];
        arr = new int[width][hight];
        for (int i = 0; i < width; i++)
            for (int j = 0; j < hight; j++)
                arr[i][j] = buffer[readPoint++];
    }

    public LED(int width, int hight, int x, int y) {
        super(x, y);
        this.width = width;
        this.hight = hight;
        arr = new int[width][hight];
        for (int i = 0; i < arr.length; i++)
            Arrays.fill(arr[i], -1);
    }

    public void set(int x, int y, int color){
        if (arr.length <= x | arr[x].length <= y)
            return;
        arr[x][y] = color;
    }

    public void set(int arr[][]){
        if (arr.length != this.arr.length | arr[0].length != this.arr[0].length)
            return;
        this.arr = arr.clone();
    }

    public int[][] get(){
        return arr;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Size:");
        buffer.append(width);
        buffer.append('/');
        buffer.append(hight);
        buffer.append(super.toString());
        buffer.append('\n');
        for (int i = 0; i < arr.length; i++){
            buffer.append('{');
            for (int j = 0; j < arr.length; j++){
                buffer.append(arr[i][j]);
                buffer.append(',');
            }
            buffer.append("}\n");
        }
        return buffer.toString();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        LED led = new LED(width, hight,x, y);
        led.arr = arr.clone();
        return led;
    }

    @Override
    public byte[] toByte() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write(HEAD.getBytes(), 0, HEAD.length());
        byte[] buffer = super.toByte();
        write(stream, buffer);
        stream.write(width);
        stream.write(hight);
        for (int i = 0; i < arr.length; i++)
            for (int j = 0; j < arr[i].length; j++)
                stream.write(arr[i][j]);
        return stream.toByteArray();
    }

    public static int getBufferSize(int width, int hight){
        int size = HEAD.length() + BUFFER_SIZE + 2;
        return size + (width * hight);
    }

    public static boolean isFile(byte[] buffer){
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++)
            b[i] = buffer[i];
        if (!HEAD.equals(toByteString(b)))
            return false;
        return true;
    }


}