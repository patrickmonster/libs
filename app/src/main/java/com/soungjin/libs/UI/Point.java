package com.soungjin.libs.UI;

import com.soungjin.libs.Data.PadFile;

import java.io.ByteArrayOutputStream;

/**
 * Created by RyuSoungJin on 2016-07-05.
 */
public class Point extends PadFile {

    public static final int BUFFER_SIZE = 6;
    public static final String HEAD = "POIN";

    public int x, y;

    public Point(byte[] buffer) {
        super();
        if (!isFile(buffer))
            getError();
        x = buffer[4];
        y = buffer[5];
    }

    public Point(int x, int y) {
        super();
        this.x = x;
        this.y = y;
    }

    public static boolean isFile(byte[] buffer){
        if (buffer.length != BUFFER_SIZE)
            return false;
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++)
            b[i] = buffer[i];
        if (!HEAD.equals(toByteString(b)))
            return false;
        return true;
    }

    public void ChangePoint(int x, int y){
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "Point:" + x + "/" + y;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new Point(x, y);
    }

    public static byte[] getByteToLingth(byte[] buffer, int start, int length){
        byte[] tmp = new byte[length];
        System.arraycopy(buffer, start, tmp, 0 ,length);
        return tmp;
    }

    public static void write(ByteArrayOutputStream stream, byte[] buffer){
        stream.write(buffer, 0, buffer.length);
    }

    public byte[] toByte(){
        ByteArrayOutputStream stream = new ByteArrayOutputStream(BUFFER_SIZE);
        stream.write(HEAD.getBytes(), 0, HEAD.length());
        stream.write(x);
        stream.write(y);
        return stream.toByteArray();
    }
}