package com.iceniro.ticket.dataProcess;

import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author gan.jiangwei
 * @since 2018/9/3 0003.
 */
public class StationStore {

    public static Map<String, String> stationAlis;

    static {
        stationAlis = new HashMap<String, String>();
        List<String> stations = FileUitl.readFile();
        for (String station : stations) {
            String[] valuesPaire = station.split(":");
            stationAlis.put(valuesPaire[0],valuesPaire[1]);
        }
    }

    public static String getName(String key){
        return stationAlis.get(key);
    }

    public static String getKey(String name){
        Set<Map.Entry<String, String>> entries = stationAlis.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            String ali = entry.getKey();
           if(entry.getValue().equals(name)){
               return ali;
           }
        }
        return null;
    }
}
