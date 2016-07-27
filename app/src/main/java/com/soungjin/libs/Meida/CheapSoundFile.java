/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.soungjin.libs.Meida;

/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;

public class CheapSoundFile {
    public interface ProgressListener {
        boolean reportProgress(double fractionComplete);
    }

    public interface Factory {
        public CheapSoundFile create();
        public String[] getSupportedExtensions();
    }

    static Factory[] sSubclassFactories = new Factory[] {
            CheapWAV.getFactory()
    };

    static ArrayList<String> sSupportedExtensions = new ArrayList<String>();
    static HashMap<String, Factory> sExtensionMap = new HashMap<String, Factory>();

    static {
        for (Factory f : sSubclassFactories) {
            for (String extension : f.getSupportedExtensions()) {
                sSupportedExtensions.add(extension);
                sExtensionMap.put(extension, f);
            }
        }
    }

    /**
     * Static method to create the appropriate CheapSoundFile subclass
     * given a filename.
     *
     * TODO: make this more modular rather than hardcoding the logic
     */
    public static CheapSoundFile create(String fileName,
                                        ProgressListener progressListener)
            throws java.io.FileNotFoundException,
            java.io.IOException {
        File f = new File(fileName);
        if (!f.exists()) {
            throw new java.io.FileNotFoundException(fileName);
        }
        String name = f.getName().toLowerCase();
        String[] components = name.split("\\.");
        if (components.length < 2) {
            return null;
        }
        Factory factory = sExtensionMap.get(components[components.length - 1]);
        if (factory == null) {
            return null;
        }
        CheapSoundFile soundFile = factory.create();
        soundFile.setProgressListener(progressListener);
//        soundFile.ReadFile(f);
        return soundFile;
    }

    public static boolean isFilenameSupported(String filename) {
        String[] components = filename.toLowerCase().split("\\.");
        if (components.length < 2) {
            return false;
        }
        return sExtensionMap.containsKey(components[components.length - 1]);
    }

    /**
     * Return the filename extensions that are recognized by one of
     * our subclasses.
     */
    public static String[] getSupportedExtensions() {
        return sSupportedExtensions.toArray(
                new String[sSupportedExtensions.size()]);
    }

    protected ProgressListener mProgressListener = null;
    protected File mInputFile = null;
    protected FrameAudioData[] FrameGains;
    protected int[] mFrameOffsets;
    protected int[] mFrameLens;
    protected int mNumFrames;
    public static int sampleRate = 44100;//고정 셈플레이트
    public static int channels = 2;

    protected CheapSoundFile() {
    }

    public void ReadFile(File inputFile)
            throws java.io.FileNotFoundException,
            java.io.IOException {
        mInputFile = inputFile;
    }

    public void setProgressListener(ProgressListener progressListener) {
        mProgressListener = progressListener;
    }

    public int getNumFrames() {
        return 0;
    }

    public int getSamplesPerFrame() {
        return 0;
    }

    public int[] getFrameOffsets() {
        return null;
    }

    public int[] getFrameLens() {
        return null;
    }

    public int[] getFrameGains() {
        return null;
    }

    public int getFileSizeBytes() {
        return 0;
    }

    public int getAvgBitrateKbps() {
        return 0;
    }

    public int getSampleRate() {
        return 0;
    }

    public int getChannels() {
        return 0;
    }

    public String getFiletype() {
        return "Unknown";
    }

    /**
     * If and only if this particular file format supports seeking
     * directly into the middle of the file without reading the rest of
     * the header, this returns the byte offset of the given frame,
     * otherwise returns -1.
     */
    public int getSeekableFrameOffset(int frame) {
        return -1;
    }

    private static final char[] HEX_CHARS = {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
    public static String bytesToHex (byte hash[]) {
        char buf[] = new char[hash.length * 2];
        for (int i = 0, x = 0; i < hash.length; i++) {
            buf[x++] = HEX_CHARS[(hash[i] >>> 4) & 0xf];
            buf[x++] = HEX_CHARS[hash[i] & 0xf];
        }
        return new String(buf);
    }

    public String computeMd5OfFirst10Frames()
            throws java.io.FileNotFoundException,
            java.io.IOException,
            java.security.NoSuchAlgorithmException {
        int[] frameOffsets = getFrameOffsets();
        int[] frameLens = getFrameLens();
        int numFrames = frameLens.length;
        if (numFrames > 10) {
            numFrames = 10;
        }

        MessageDigest digest = MessageDigest.getInstance("MD5");
        FileInputStream in = new FileInputStream(mInputFile);
        int pos = 0;
        for (int i = 0; i < numFrames; i++) {
            int skip = frameOffsets[i] - pos;
            int len = frameLens[i];
            if (skip > 0) {
                in.skip(skip);
                pos += skip;
            }
            byte[] buffer = new byte[len];
            in.read(buffer, 0, len);
            digest.update(buffer);
            pos += len;
        }
        in.close();
        byte[] hash = digest.digest();
        return bytesToHex(hash);
    }

    public void WriteFile(File outputFile, int startFrame, int numFrames)
            throws java.io.IOException {
    }

    public int getData(byte arr[], int start, int end){
        byte data[] = new byte[end - start];
        for (int i = 0; i < data.length; i++)
            data[i] = arr[start + i];
        return getData(data);
    }

    public int getData(byte arr[]){
        return  ((0xff & arr[3]) << 24) |
                ((0xff & arr[2]) << 16) |
                ((0xff & arr[1]) << 8) |
                ((0xff & arr[0]));
    }

    public byte[] getData(int data){
        return new byte[]{
                (byte) (data & 0xff),
                (byte) ((data >> 8) & 0xff),
                (byte) ((data >> 16) & 0xff),
                (byte) ((data >> 24) & 0xff)
        };
    }


    public static short get2Data(byte arr[], int start, int end){
        byte data[] = new byte[end - start];
        for (int i = 0; i < data.length; i++)
            data[i] = arr[start + i];
        return get2Data(data);
    }

    public static byte[] get2Data(short data){
        return new byte[]{
                (byte) (data & 0xff),
                (byte) ((data >> 8) & 0xff)};
    }

    public static short get2Data(byte arr[]){
        return (short) (((0xff & arr[1]) << 8) |
                ((0xff & arr[0])));
    }

    public void WritePCM() throws java.io.IOException{
        WritePCM(mInputFile.getPath() + ".pcm");
    }

    public void WritePCM(String outputFile) throws java.io.IOException{//44100Hz / 2
        File f = new File(outputFile);
        FileOutputStream out = new FileOutputStream(outputFile);
        for (FrameAudioData data : FrameGains){
            FrameAudioData d = ConVert.C(data, sampleRate);
            for (int i = 0; i < d.length; i++){
                out.write(get2Data(d.LeftData[i]));
                if (!d.isMono)
                    out.write(get2Data(d.RiteData[i]));
                else out.write(get2Data(d.LeftData[i]));
            }
        }
        out.close();
    }


    public static class FrameAudioData{
        short RiteData[], LeftData[];
        int length;
        boolean isMono;
        int sampleRate;

        public FrameAudioData(int size, boolean isMono){
            length = size;
            RiteData = new short[size];
            LeftData = new short[size];
            this.isMono = isMono;
        }
        public short[] getData(int index) {
            if (index >= length || index < 0)
                return null;
            return new short[]{RiteData[index], LeftData[index]};
        }
    }

    public static class ConVert{
        public static FrameAudioData C(FrameAudioData in, int hz){
            if (in.sampleRate == hz)
                return in;
            FrameAudioData data = new FrameAudioData(in.length, in.isMono);
            int sample = in.sampleRate/(in.sampleRate - hz);
            int max_Size = Math.max(hz, in.sampleRate);
            int sample_count = Math.abs(sample);
            if (sample_count == 0)
                return in;
            for (int i = 0; i < max_Size ;i++){
                if (data.isMono){
                    if (i % sample == 0){
                        if (sample > 0)
                            data.LeftData[i] = in.LeftData[i/sample_count];
                        continue;
                    }
                    if (sample > 0)
                        data.LeftData[i] = in.LeftData[i/sample_count];
                    else data.LeftData[i/sample_count] = in.LeftData[i];
                }else{
                    if (i % sample == 0){
                        if (sample > 0){
                            data.RiteData[i] = in.RiteData[i/sample_count];
                            data.LeftData[i] = in.LeftData[i/sample_count];
                        }
                        continue;
                    }
                    if (sample > 0){
                        data.RiteData[i] = in.RiteData[i/sample_count];
                        data.LeftData[i] = in.LeftData[i/sample_count];
                    }
                    else{
                        data.RiteData[i/sample_count] = in.RiteData[i];
                        data.LeftData[i/sample_count] = in.LeftData[i];
                    }
                }
            }
            return data;
        }
    }
};
