package com.soundwallcontroller;

import android.content.SharedPreferences;
import android.util.Log;

public class ChannelData {
    public class Channel{
        public Channel(float al, float ar, float bl, float br){
            audioALeft = al;
            audioARight = ar;
            audioBLeft = bl;
            audioBRight = br;
        }
        float getAudioALeft(){ return audioALeft * ((float)value / 256.0f);}
        float getAudioARight(){ return audioARight * ((float)value / 256.0f);}
        float getAudioBLeft(){ return audioBLeft * ((float)value / 256.0f);}
        float getAudioBRight(){ return audioBRight * ((float)value / 256.0f);}

        public int value = 0;
        float audioALeft = 0;
        float audioARight = 0;
        float audioBLeft = 0;
        float audioBRight = 0;
        boolean isPlaying = false;
        float virtualPosX = 0.5f;
        float virtualPosY = 0.5f;
    };
    public Channel[] channels = {
            new Channel(1.0f, 0.0f, 0.0f, 0.0f),
            new Channel(1.0f, 1.0f, 0.0f, 0.0f),
            new Channel(1.0f, 1.0f, 0.0f, 0.0f),
            new Channel(0.0f, 1.0f, 0.0f, 0.0f),
            new Channel(0.0f, 1.0f, 0.0f, 0.0f),
            new Channel(1.0f, 0.0f, 0.0f, 0.0f),
            new Channel(1.0f, 0.0f, 0.0f, 0.0f),
            new Channel(1.0f, 0.0f, 0.0f, 0.0f)
    };
    public float getChannelMix(int idx, int speaker){
        if(idx < 0)return 0;
        if(idx > channels.length-1)return 0;
        switch (speaker){
            case 0: return channels[idx].audioALeft;
            case 1: return channels[idx].audioARight;
            case 2: return channels[idx].audioBLeft;
            case 3: return channels[idx].audioBRight;
        }
        return 0;
    }
    public void setChannelMix(int idx, int speaker, float val){
        if(idx < 0)return;
        if(idx > channels.length-1)return;
        if(val < 0)val = 0.0f;
        if(val > 1)val = 1.0f;
        switch (speaker){
            case 0: channels[idx].audioALeft = val; break;
            case 1: channels[idx].audioARight = val; break;
            case 2: channels[idx].audioBLeft = val; break;
            case 3: channels[idx].audioBRight = val; break;
        }
    }
    public void setChannelValue(int index){
        if(index < 0)return;
        if(index > channels.length-1)return;
        setChannelValue(index, channels[index].value);
    }
    public void setChannelValue(int index, int value){
        if(index < 0)return;
        if(index > channels.length-1)return;
        channels[index].value = value;
        float fval = (float)value / 256.0f;
        soundChannelMixJNI(index,
                channels[index].audioALeft * fval,
                channels[index].audioARight * fval,
                channels[index].audioBLeft * fval,
                channels[index].audioBRight * fval
                );
    }
    public void playStop(int index, boolean play){
        if(play){
            playSoundJNI(index);
            setChannelValue(index);
            channels[index].isPlaying = true;
        }else{
            stopSoundJNI(index);
            channels[index].isPlaying = false;
        }
    }
    public boolean isPlaying(int index){
        return isPlayingSoundJNI(index);
    }
    public void loadSettings(SharedPreferences settings){
        for(int i=0; i<8; i++){
            channels[i].audioALeft = settings.getFloat("ch"+Integer.toString(i)+"ALeft", channels[i].audioALeft);
            channels[i].audioARight = settings.getFloat("ch"+Integer.toString(i)+"ARight", channels[i].audioARight);
            channels[i].audioBLeft = settings.getFloat("ch"+Integer.toString(i)+"BLeft", channels[i].audioBLeft);
            channels[i].audioBRight = settings.getFloat("ch"+Integer.toString(i)+"BRight", channels[i].audioBRight);
            channels[i].isPlaying = settings.getBoolean("ch"+Integer.toString(i)+"Playing", channels[i].isPlaying);
            playStop(i, channels[i].isPlaying);
        }
    }
    public void saveSettings(SharedPreferences.Editor editor){
        for(int i=0; i<8; i++){
            editor.putFloat("ch"+Integer.toString(i)+"ALeft", channels[i].audioALeft);
            editor.putFloat("ch"+Integer.toString(i)+"ARight", channels[i].audioARight);
            editor.putFloat("ch"+Integer.toString(i)+"BLeft", channels[i].audioBLeft);
            editor.putFloat("ch"+Integer.toString(i)+"BRight", channels[i].audioBRight);
            editor.putBoolean("ch"+Integer.toString(i)+"Playing", channels[i].isPlaying);
        }
    }
    public void calculateVirtualPosition(int index){
        if(index < 0)return;
        if(index > channels.length-1)return;

        // 1:0 = 1
        // 0:1 = -1
        // 1:1 = 0
        // 0:0 = 0
        // pos = 0.5f - ((right - left) * 0.5f);
        //
        float p0_x = 0.5f - ((getChannelMix(index, 0) - getChannelMix(index, 1)) * 0.5f);
//
//        float al = getChannelMix(index, 0);
//        float ar = getChannelMix(index, 1);
//
//        Log.i("vpos", p0_x+" | "+al+", "+ar);

        float p1_x = 0.5f - ((getChannelMix(index, 2) - getChannelMix(index, 3)) * 0.5f);
        float p2_y = 0.5f - ((getChannelMix(index, 0) - getChannelMix(index, 2)) * 0.5f);
        float p3_y = 0.5f - ((getChannelMix(index, 1) - getChannelMix(index, 3)) * 0.5f);
//
//        Log.i("vpos", p0_x+","+p1_x+" | "+p2_y+","+p3_y);

//        //p0:p1 - vertical
//        //p2:p3 - horizontal
//
//        float p0_x = 0.5;
//        float p1_x = 0.5;
//        float p2_y = 0.5;
//        float p3_y = 0.5;

        float s1_x, s1_y, s2_x, s2_y;
        s1_x = p1_x - p0_x;
        s2_y = p3_y - p2_y;
        float s, t;
        s = (-1.0f * p0_x + s1_x * -p2_y) / (-1.0f + s1_x * s2_y);
        t = (1.0f * -p2_y - s2_y * p0_x) / (-1.0f + s1_x * s2_y);

//        Log.i("pos", s+","+t);

        if(Float.isNaN(s) || Float.isNaN(t)){
            channels[index].virtualPosX = 0.5f;
            channels[index].virtualPosY = 0.5f;
        }else{
            channels[index].virtualPosX = s;
            channels[index].virtualPosY = t;
        }
    }

    public float getVirtualPosX(int index){
        if(index < 0)return 0;
        if(index > channels.length-1)return 0;

        return channels[index].virtualPosX;
    }
    public float getVirtualPosY(int index){
        if(index < 0)return 0;
        if(index > channels.length-1)return 0;

        return channels[index].virtualPosY;
    }

    public native int loadSoundJNI(String filename, int index);
    private native int playSoundJNI(int index);
    private native int stopSoundJNI(int index);
    private native int soundChannelMixJNI(int index, float Al, float Ar, float Bl, float Br);
    private native boolean isPlayingSoundJNI(int index);
}
