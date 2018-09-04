package com.iceniro.ticket.bean;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author gan.jiangwei
 * @since 2018/9/3 0003.
 */
public class TrainDetail {

    public String MUCH_MORE = "有";
    public List<String> throwByStations;
    public String trainNo;
    public String trainCode;
    public String begin;
    public String end;
    public String onBorad;
    public String onBoradIndex;
    public String getOff;
    public String getOffIndex;
    public Date onBoradTime;
    public String onBoradTimeStr;
    public Date getOffTime;
    public String getOffTimeStr;
    public String trainDate;
    public int secondSetNum;
    public String costTimes;
    public String setTypes;
    //软卧
    public int softBed;
    //硬卧
    public int hardBed;
    //软座
    public int softSet;
    //硬座
    public int hardSet;

    public TrainDetail(String[] detail) {
        try {
            //列车代号
            this.trainNo = detail[2];
            //列车编号
            this.trainCode = detail[3];
            //始发站
            this.begin = detail[4];
            //终点站
            this.end = detail[5];
            //上车站
            this.onBorad = detail[6];
            this.onBoradIndex = detail[16];
            //目的地
            this.getOff = detail[7];
            this.getOffIndex = detail[17];
            //座位类型
            this.setTypes = detail[35];
            //二等座数量
            this.secondSetNum = parseSetNum(detail[30]);
            //软卧 23
            this.softBed = parseSetNum(detail[23]);
            //硬卧 26
            this.hardBed = parseSetNum(detail[28]);
            //软座 28
            this.softSet = parseSetNum(detail[27]);
            //硬座 29
            this.hardSet = parseSetNum(detail[29]);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
            String date = detail[13];
            this.trainDate = date;
            String startTime = detail[8];
            this.onBoradTime = sdf.parse(date + " " + startTime + ":00");
            this.onBoradTimeStr = date + " " + startTime;
            String endTime = detail[9];
            this.getOffTimeStr = date + " " + endTime;
            this.costTimes = detail[10];
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int parseSetNum(String secondSet) {
        if ("".equals(secondSet)) {
            return 0;
        }
        if (MUCH_MORE.equals(secondSet)) {
           return 200;
        } else if (secondSet.matches("\\d*")) {
            return Integer.parseInt(secondSet);
        }
        return 0;
    }

    public boolean hasSetRemain() {
        if (this.trainCode.startsWith("D") || this.trainCode.startsWith("D")) {
            return this.secondSetNum > 2;
        }
        if (this.trainCode.startsWith("K")) {
            return this.softBed > 2 || this.hardBed > 2;
        }
        return false;
    }

    public String getRemainSetNum(){
        if (this.trainCode.startsWith("D") || this.trainCode.startsWith("D")) {
            return String.valueOf(this.secondSetNum);
        }
        if (this.trainCode.startsWith("K")) {
            return "软卧：" + this.softBed + ";硬卧：" + this.hardBed;
        }
        return "0";
    }

    public void setThrowByStations(JSONArray array){
        List<String> stationNames = new ArrayList<String>();
        for (Object item : array) {
            String currentStation = getStationName(item);
            stationNames.add(currentStation);
        }
        this.throwByStations = stationNames;
    }

    private static String getStationName(Object item) {
        JSONObject jsonObject = (JSONObject) item;
        Map<String, Object> stationInfo = jsonObject.getInnerMap();
        return (String) stationInfo.get("station_name");
    }

    public boolean isStationContain(String onBoradName,String getOffName){
        boolean before = false,after = false;
        for(String stations : throwByStations){
            if(before || stations.startsWith(onBoradName) || stations.contains(onBoradName)){
                before = true;
            }
            if(after || stations.startsWith(getOffName) || stations.contains(getOffName)){
                after = true;
            }
        }
        return before && after;
    }
}
