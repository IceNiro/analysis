package com.iceniro.ticket.dataProcess;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iceniro.ticket.bean.TrainDetail;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author gan.jiangwei
 * @since 2018/9/3 0003.
 */
public class DeviousAnalysis {

    private final static Logger logger = LogManager.getLogger(DeviousAnalysis.class);

    private static ExecutorService pool = Executors.newFixedThreadPool(3);

    private static transient AtomicInteger transNum = new AtomicInteger(0);

    public static void setTransNum(int num) {
        transNum.set(num);
    }

    public static void decreaseTrainNum(){
        int left = transNum.decrementAndGet();
        if (left == 0) {
            logger.error("所有车辆分析已完成！！！");
            System.exit(-1);
            DataProcess.main(new String[]{"a"});
        }
    }

    /**
     * 查询列车途径站点
     * 对上车站前 或 目的 后面的站 重新发起 查询
     *
     * @param detail 车辆信息
     */
    public static void deviousAnalysis(TrainDetail detail, boolean doAnalysis) {
        pool.submit(new AnalysisTask(detail, doAnalysis));
    }

    private static class AnalysisTask implements Runnable {
        String from, to;
        boolean doAnalysis;
        String trainCode;
        int analysisLevel = 1;
        List<String> stationNames;

        public AnalysisTask(TrainDetail detail, boolean doAnalysis) {
            this.from = detail.onBorad;
            this.to = detail.getOff;
            this.doAnalysis = doAnalysis;
            this.trainCode = detail.trainCode;
            this.stationNames = detail.throwByStations;
        }

        public void run() {
            String fromName = StationStore.getName(from);
            String toName = StationStore.getName(to);
            boolean before = true, after = false;

            List<String> beforeStations = new ArrayList<String>(), afterStations = new ArrayList<String>();
            for (String currentStation : stationNames) {
                if (fromName.equals(currentStation)) {
                    before = false;
                }
                if (before) {
                    beforeStations.add(currentStation);
                }
                if (after) {
                    afterStations.add(currentStation);
                }
                if (toName.equals(currentStation)) {
                    after = true;
                }
            }
            logger.info("{}途径站点查询开始....", trainCode);
            doDeviousBefore(beforeStations, to);
            doDeviousAfter(from, afterStations);
            logger.info("{}途径站点查询已经完成,还剩{}趟", trainCode, transNum.get() - 1);
            decreaseTrainNum();
            if (analysisLevel == 2) {
                doBetweenContinue();
            }
        }

        private void doBetweenContinue() {
            int indexFrom = stationNames.indexOf(StationStore.getName(from));
            int indexTo = stationNames.indexOf(StationStore.getName(to));
            if (indexTo - indexFrom > 8) {
                //上车站 后面N个站
                List<String> aheadStations = new ArrayList<String>(2);
                //目的地前面N个站
                List<String> tailStation = new ArrayList<String>(3);
                aheadStations.add(stationNames.get(indexFrom + 1));
                aheadStations.add(stationNames.get(indexFrom + 2));
                aheadStations.add(stationNames.get(indexTo - 1));
                tailStation.add(stationNames.get(indexTo - 2));
                tailStation.add(stationNames.get(indexTo - 3));
            }
        }
    }

    private static String getStationName(Object item) {
        JSONObject jsonObject = (JSONObject) item;
        Map<String, Object> stationInfo = jsonObject.getInnerMap();
        return (String) stationInfo.get("station_name");
    }

    private static void doDeviousAfter(String from, List<String> afterStations) {
        for (String stationName : afterStations) {
            DataProcess.eachTrainHasRemain(from, StationStore.getKey(stationName), false);
        }
    }

    private static void doDeviousBefore(List<String> beforeStations, String to) {
        for (String stationName : beforeStations) {
            DataProcess.eachTrainHasRemain(StationStore.getKey(stationName), to, false);
        }
    }
}
