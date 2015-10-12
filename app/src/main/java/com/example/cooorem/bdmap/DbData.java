package com.example.cooorem.bdmap;

import java.io.StringReader;

/**
 * Created by Administrator on 2015/10/9.
 */
public class DbData {
    private String description,localNear;
    private String averageDb;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    private String time;
    private double latitude,longitude;

    public DbData(){
        description=localNear=null;
        latitude=longitude=0;
        averageDb=null;
    }

    @Override
    public String toString() {
        StringBuilder sb=new StringBuilder();
        sb.append(description+"\n");
        sb.append(localNear+"\n");
        sb.append(averageDb+"\n纬度"+latitude+" 经度"+longitude+"\n");
        sb.append("时间"+time);
        return sb.toString();
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLocalNear(String localNear) {
        this.localNear = localNear;
    }


    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getDescription() {
        return description;
    }

    public String getLocalNear() {
        return localNear;
    }

    public void setAverageDb(String averageDb) {
        this.averageDb = averageDb;
    }

    public String getAverageDb() {

        return averageDb;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
