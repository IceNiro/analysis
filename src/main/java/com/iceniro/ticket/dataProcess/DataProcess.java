package com.iceniro.ticket.dataProcess;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iceniro.ticket.HttpUtil.HttpUtil;
import com.iceniro.ticket.bean.TrainDetail;
import com.iceniro.ticket.exception.ExceptionUtil;
import com.iceniro.ticket.notice.DingMsgSender;
import com.sun.jmx.remote.internal.ArrayQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author gan.jiangwei
 * @since 2018/9/3 0003.
 */
public class DataProcess {

    private final static Logger logger = LogManager.getLogger(DataProcess.class);
    private final static List<String> cache = new ArrayQueue<String>(3000);
    static String GET_ONBOARD = "杭州";
    static String GET_OFF = "长沙";
    static String date = "2018-10-01";
    static int DATE_BEFORE = 3;
    static boolean STATION_CACHE = true;
    static boolean SEND_DING_MSG = false;
    //指定车辆
    static String SPECIAL_TRAIN_LIST = "";
    static ExecutorService printThread = Executors.newFixedThreadPool(2);
    static Date passTime;
    static Date GET_OFF_TIME;


    private static boolean timeSuitablt(Date onBoradTime,Date getOffTime) {
        if (DATE_BEFORE == 1) {
            return getOffTime.before(GET_OFF_TIME);
        } else if(DATE_BEFORE == 2){
            return onBoradTime.after(passTime);
        }
        return true;
    }

    public static void main(String[] args) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            passTime = sdf.parse(date + " 21:30:00");
            GET_OFF_TIME = sdf.parse(date + " 13:30:00");
            logger.error("{}到{}车票情况分析开始。。。", GET_ONBOARD, GET_OFF);
            eachTrainHasRemain(StationStore.getKey(GET_ONBOARD), StationStore.getKey(GET_OFF), true);
            logger.info("{}到{}车票情况分析结束，请查看日志", GET_ONBOARD, GET_OFF);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询 start 站到 off 直接的列车列表
     * 是否有余票
     *
     * @param start 上车车站code
     * @param off   目的地车站code
     */
    public static void eachTrainHasRemain(String start, String off, boolean doAnalysis) throws InterruptedException {
        if (STATION_CACHE && hasQueried(start + off)) {
            logger.info("开始查询：{}到{}的列车，PASSED...", StationStore.getName(start), StationStore.getName(off));
            return;
        }
        logger.warn("开始查询：{}到{}的列车", StationStore.getName(start), StationStore.getName(off));
        JSONObject jsonObject = HttpUtil.getTrainList(date, start, off);
        if (jsonObject == null) {
            logger.error("{}到{}的列车,无法获取数据", StationStore.getName(start), StationStore.getName(off));
            return;
        }
        String trainsStr = jsonObject.getJSONObject("data").getString("result");
        String[] trains = trainsStr.split(",");
        if (doAnalysis) {
            logger.error("一共{}趟列车", trains.length);
            if(!"".equals(SPECIAL_TRAIN_LIST)){
                logger.error("只分析部分车：{}",SPECIAL_TRAIN_LIST);
                DeviousAnalysis.setTransNum(SPECIAL_TRAIN_LIST.split(",").length);
            }else{
                DeviousAnalysis.setTransNum(trains.length);
            }
        }
        for (String train : trains) {
            try {
                processSigleTrain(train, doAnalysis);
            } catch (Exception e) {
                logger.info("error:{}", ExceptionUtil.getMessage(e));
                DeviousAnalysis.decreaseTrainNum();
            }
        }
        /*if(!doAnalysis){
            Thread.sleep(500);
            eachTrainHasRemain(start,off,false);
        }*/
    }

    private static void processSigleTrain(String train, boolean doAnalysis) {
        String[] trainDetail = train.split("\\|");
        TrainDetail detail = new TrainDetail(trainDetail);
        if (detail.trainNo == null) {
            return;
        }
        //指定车辆查询
        if (!"".equals(SPECIAL_TRAIN_LIST) && !SPECIAL_TRAIN_LIST.contains(detail.trainCode)) {
            return;
        }
        boolean passByStationQueried = false;
        if (detail.hasSetRemain()) {
            /*if(Double.parseDouble(detail.costTimes) > COST_LIMIT){
                return;
            }*/
            logger.warn("列车信息：{}",detail.toString());
            //查询列车途径站点
            JSONObject trainsStations = HttpUtil.testAvoidS(HttpUtil.buildQueryTrainParamStr(detail.trainNo, detail.onBorad, detail.getOff, date));
            if (trainsStations == null) {
                return;
            }
            JSONArray stations = trainsStations.getJSONObject("data").getObject("data", JSONArray.class);
            detail.setThrowByStations(stations);
            passByStationQueried = true;
            if (detail.isStationContain(GET_ONBOARD, GET_OFF)) {
                printThread.submit(new PrintTask(detail));
            }
            logger.warn("列车不能站点不匹配");
        }
        if (doAnalysis) {
            if (!passByStationQueried) {
                //查询列车途径站点
                JSONObject trainsStations = HttpUtil.testAvoidS(HttpUtil.buildQueryTrainParamStr(detail.trainNo, detail.onBorad, detail.getOff, date));
                if (trainsStations == null) {
                    return;
                }
                JSONArray stations = trainsStations.getJSONObject("data").getObject("data", JSONArray.class);
                detail.setThrowByStations(stations);
            }
            logger.warn("开始查询{}列车途径站点", detail.trainCode);
            DeviousAnalysis.deviousAnalysis(detail, doAnalysis);
        }
    }

    private static boolean hasQueried(String s) {
        synchronized (cache) {
            if (cache.contains(s)) {
                return true;
            }
            cache.add(s);
            return false;
        }
    }

    static class PrintTask implements Runnable {
        TrainDetail detail;

        public PrintTask(TrainDetail detail) {
            this.detail = detail;
        }

        public static void printTrainInfo(TrainDetail detail) {
            JSONObject trainsStations = HttpUtil.queryTrainPrice(detail.trainNo, detail.onBoradIndex, detail.getOffIndex, detail.setTypes, date);
            String prices = "未知";
            if (trainsStations != null) {
                Map<String, Object> trainsPrice = trainsStations.getJSONObject("data").getInnerMap();
                prices = detail.trainCode.startsWith("K") ? getNormalPrice(trainsPrice) : getDGPrice(trainsPrice);
            }
            if (timeSuitablt(detail.onBoradTime,detail.getOffTime)) {
                String context = String.format("%1s到 %2s的:%3s列车有余票，票数：%4s，上车时间：%5s,到达时间：%8s,历时：%6s,票价：%7s", StationStore.getName(detail.onBorad),
                        StationStore.getName(detail.getOff), detail.trainCode, detail.getRemainSetNum(), detail.onBoradTimeStr, detail.costTimes, prices,detail.getOffTimeStr);
                logger.error("{} 到 {}的:{}列车有余票，票数：{}，上车时间：{},到达时间：{},历时：{},票价：{}", StationStore.getName(detail.onBorad),
                        StationStore.getName(detail.getOff), detail.trainCode, detail.getRemainSetNum(), detail.onBoradTimeStr,detail.getOffTimeStr, detail.costTimes, prices);
                if(SEND_DING_MSG) {
                    DingMsgSender.sendDingMsgDirect(context);
                }
            }
        }

        private static String getNormalPrice(Map<String,Object> priceInfo){
            return "软卧:" + priceInfo.get("A4") + ",硬卧：" + priceInfo.get("A3") + "硬座:" +  priceInfo.get("A1");
        }

        private static String getDGPrice(Map<String,Object> priceInfo){
            return "二等座:" + priceInfo.get("0") + ",一等座：" + priceInfo.get("M");
        }
        public void run() {
            printTrainInfo(detail);
        }
    }

}

