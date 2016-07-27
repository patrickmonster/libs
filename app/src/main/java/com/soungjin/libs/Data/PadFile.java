package com.soungjin.libs.Data;

/**
 * Created by RyuSoungJin on 2016-07-07.
 */
public abstract class PadFile {

    public PadFile(byte[] buffer) {
        super();
    }

    public PadFile() {
        super();
    }

    public abstract byte[] toByte();

    public static String toByteString(byte[] buff){
        return toByteString(buff, 0, buff.length);
    }

    public static String toByteString(byte[] buff,int start, int end){
        return new String(buff, start, end);
    }

    public void getError(){
        throw new NullPointerException("일치하는 파일이 아님!\n사용전 isFile(byte)로 확인바람!");
    }
}