package com.iceniro.ticket.notice;

import com.alibaba.fastjson.JSON;
import com.sun.jmx.remote.internal.ArrayQueue;
import org.apache.http.HttpHeaders;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 钉钉消息发送
 * Created by gjw on 2018/5/21 0021.
 */
public class DingMsgSender {


    private static final String url = "https://oapi.dingtalk.com/robot/send";
    private static final String TOKEN = "d63ec63bbac68d52a4b980e9393f3a400cce0f9a3b08c8b6c95509afafff59f8";
    static Map<String,Long> sendMsgCache = new ConcurrentHashMap<>();

    /**
     * 钉钉发送消息
     *
     * @param text 内容
     */
    public static void sendDingMsgDirect(String text) {
        if (!contextReapter(text)) {
            return;
        }
        Map<String, String> body = new HashMap<String, String>();
        body.put("title", "监控通知");
        body.put("text", text);
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("msgtype", "markdown");
        paramMap.put("markdown", body);
        String json = JSON.toJSONString(paramMap);
        String url = "https://oapi.dingtalk.com/robot/send";
        HttpPost post = new HttpPost(url + "?access_token=" + TOKEN);
        post.setEntity(new StringEntity(json, "UTF-8"));
        post.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(1000)
                .setConnectionRequestTimeout(1000)
                .setSocketTimeout(1000).build();
        post.setConfig(requestConfig);
        HttpClient client = HttpClients.createDefault();
        try {
            client.execute(post);
        } catch (IOException e) {
        }
    }

    private static boolean contextReapter(String text) {
        long curren = System.currentTimeMillis();
        Long cachedTime;
        if ((cachedTime = sendMsgCache.get(text)) == null) {
            sendMsgCache.put(text,curren);
            return true;
        }
        if(curren - cachedTime < 3 * 60 * 1000){
            return false;
        }
        sendMsgCache.replace(text,curren);
        return true
                ;
    }

    protected void sendDingMsg(String token, String name, String sendSN) {

        Map<String, Object> paramMap = buildLinkParams(name, sendSN);
        send(paramMap, token);
    }

    private Map<String, Object> buildLinkParams(String name, String sendSN) {

        Map<String, String> body = new HashMap<String, String>();
        body.put("title", name);
        body.put("text", name + "消息通知，点击查看详情");
        body.put("picUrl", "");
        body.put("messageUrl", "?sendSN=" + sendSN);
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("msgtype", "link");
        paramMap.put("link", body);
        return paramMap;
    }

    private void send(Map<String, Object> paramMap, String token) {

        String json = JSON.toJSONString(paramMap);
        HttpPost post = new HttpPost(url + "?access_token=" + TOKEN);
        post.setEntity(new StringEntity(json, "UTF-8"));
        post.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(1000)
                .setConnectionRequestTimeout(1000)
                .setSocketTimeout(1000).build();
        post.setConfig(requestConfig);
        HttpClient client = HttpClients.createDefault();
        try {
            client.execute(post);
        } catch (IOException e) {
        }
    }

}
