package edu.fandm.volkanwill.meebles;

import java.io.Serializable;

public class EnvironmentData implements Serializable {

    private int meebleCount;
    private char cityType;
    private char environmentType;
    private long time;

    public EnvironmentData(int meebleCount, char cityType, char environmentType, long time){
        this.meebleCount = meebleCount;
        this.cityType = cityType;
        this.environmentType = environmentType;
        this.time = time;
    }

    public int getMeebleCount(){
        return meebleCount;
    }

    public char getCityType(){
        return cityType;
    }

    public char getEnvironmentType(){
        return environmentType;
    }

    public long getTime(){
        return time;
    }

    public void setMeebleCount(int meebleCount){
        this.meebleCount = meebleCount;
    }

    public void setCityType(char cityType){
        this.cityType=cityType;
    }

    public void setEnvironmentType(char environmentType){
        this.environmentType=environmentType;
    }
    public void setTime(long time){
        this.time = time;
    }
}
