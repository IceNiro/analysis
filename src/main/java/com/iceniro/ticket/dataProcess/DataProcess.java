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
    static String GET_ONBOARD = "长沙";
    static String GET_OFF = "玉林";
    static String date = "2018-10-03";
    //指定车辆
    static String SPECIAL_TRAIN_LIST = "";
    static ExecutorService printThread = Executors.newFixedThreadPool(2);
    static Date passTime;


    private static boolean timeSuitablt(Date onBoradTime) {
        if ("2018-10-01".equals(date)) {
            return onBoradTime.before(passTime);
        } else {
            return onBoradTime.after(passTime);
        }
    }

    public static void main(String[] args) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            passTime = sdf.parse(date + " 18:00:00");
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
    public static void eachTrainHasRemain(String start, String off, boolean doAnalysis) {
        if (hasQueried(start + off)) {
            logger.info("开始查询：{}到{}的列车，PASSED...", StationStore.getName(start), StationStore.getName(off));
            return;
        }
        logger.info("开始查询：{}到{}的列车", StationStore.getName(start), StationStore.getName(off));
        JSONObject jsonObject = HttpUtil.getTrainList(date, start, off);
        if (jsonObject == null) {
            logger.error("{}到{}的列车,无法获取数据", StationStore.getName(start), StationStore.getName(off));
            return;
        }
        String trainsStr = jsonObject.getJSONObject("data").getString("result");
        String[] trains = trainsStr.split(",");
        if (doAnalysis) {
            logger.error("一共{}趟列车", trains.length);
            DeviousAnalysis.setTransNum(trains.length);
        }
        for (String train : trains) {
            try {
                processSigleTrain(train, doAnalysis);
            } catch (Exception e) {
                logger.info("error:{}", ExceptionUtil.getMessage(e));
                DeviousAnalysis.decreaseTrainNum();
            }
        }
    }

    private static void processSigleTrain(String train, boolean doAnalysis) {
        String[] trainDetail = train.split("\\|");
        TrainDetail detail = new TrainDetail(trainDetail);
        //指定车辆查询
        if (!"".equals(SPECIAL_TRAIN_LIST) && !SPECIAL_TRAIN_LIST.contains(detail.trainCode)) {
            return;
        }
        boolean passByStationQueried = false;
        if (detail.hasSetRemain()) {
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
            return;
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
            logger.info("开始查询{}列车途径站点", detail.trainCode);
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
                prices = detail.trainCode.startsWith("K") ? (String) trainsPrice.get("A4") : (String) trainsPrice.get("O");
            }
            if (timeSuitablt(detail.onBoradTime)) {
                String context = String.format("%1s到 %2s的:%3s列车有余票，票数：%4s，上车时间：%5s,历时：%6s,票价：%7s", StationStore.getName(detail.onBorad),
                        StationStore.getName(detail.getOff), detail.trainCode, detail.getRemainSetNum(), detail.onBoradTimeStr, detail.costTimes, prices);
                logger.error("{} 到 {}的:{}列车有余票，票数：{}，上车时间：{},历时：{},票价：{}", StationStore.getName(detail.onBorad),
                        StationStore.getName(detail.getOff), detail.trainCode, detail.getRemainSetNum(), detail.onBoradTimeStr, detail.costTimes, prices);
                DingMsgSender.sendDingMsgDirect(context);
            }
        }


        public void run() {
            printTrainInfo(detail);
        }
    }

}

