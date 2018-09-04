package com.iceniro.ticket.HttpUtil;

import com.alibaba.fastjson.JSONObject;
import com.iceniro.ticket.dataProcess.DataProcess;
import com.iceniro.ticket.exception.ExceptionUtil;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/9/2.
 */
public class HttpUtil {
    private final static Logger logger = LogManager.getLogger(HttpUtil.class);

    static String paramsStr;
    static String cookies = "JSESSIONID=D81C2FCC7CAE4C608F6BC7CAD9A3541B; ten_key=BDPjj9/QO5jZzG/LD/gd9acAGO13utuh; ten_js_key=BDPjj9%2FQO5jZzG%2FLD%2Fgd9acAGO13utuh; _jc_save_wfdc_flag=dc; _jc_save_showIns=true; _jc_save_detail=true; _jc_save_toStation=%u957F%u6C99%2CCSQ; _jc_save_fromStation=%u676D%u5DDE%2CHZH; route=6f50b51faa11b987e576cdb301e545c4; RAIL_EXPIRATION=1536313772189; RAIL_DEVICEID=tkfoViZjccel3RPTHH9rTAvcg0ztJg4F2MW4BgEKs1WpcEDZu2jds5iC4JxuohP4lNqGd9VeRoekR_T5WKhqYfKhbfSdLUR5bXhQ8jEHc8-AFBn5_wVoYmTLXBJLNxKd-jjE_5zgVxcr54VExMsOn0S6odXvBVV0; _jc_save_toDate=2018-09-04; _jc_save_fromDate=2018-09-10; BIGipServerotn=3671523594.64545.0000";

    public static String buildQueryAParamStr(String date, String from, String to) {
        // 创建参数队列
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("leftTicketDTO.train_date", date));
        params.add(new BasicNameValuePair("leftTicketDTO.from_station", from));
        params.add(new BasicNameValuePair("leftTicketDTO.to_station", to));
        params.add(new BasicNameValuePair("purpose_codes", "ADULT"));
        try {
            paramsStr = EntityUtils.toString(new UrlEncodedFormEntity(params, "UTF-8"));
            return "https://kyfw.12306.cn/otn/leftTicket/queryA?" + paramsStr;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String buildQueryTrainParamStr(String trainNo, String from, String to, String departDate) {
        // 创建参数队列
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("train_no", trainNo));
        params.add(new BasicNameValuePair("from_station_telecode", from));
        params.add(new BasicNameValuePair("to_station_telecode", to));
        params.add(new BasicNameValuePair("depart_date", departDate));
        try {
            paramsStr = EntityUtils.toString(new UrlEncodedFormEntity(params, "UTF-8"));
            return "https://kyfw.12306.cn/otn/czxx/queryByTrainNo?" + paramsStr;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String buildQueryPriceParamStr(String trainNo, String fromIndex, String toIndex, String seatTypes, String departDate, String prefix) {
        /*train_no:5l000G137740
        from_station_no:10
        to_station_no:17
        seat_types:OM9
        train_date:2018-09-30*/
        // 创建参数队列
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("train_no", trainNo));
        params.add(new BasicNameValuePair("from_station_no", fromIndex));
        params.add(new BasicNameValuePair("to_station_no", toIndex));
        params.add(new BasicNameValuePair("seat_types", seatTypes));
        params.add(new BasicNameValuePair("train_date", departDate));
        try {
            paramsStr = EntityUtils.toString(new UrlEncodedFormEntity(params, "UTF-8"));
            return prefix + paramsStr;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 发送 get请求
     */
    public static void doGet(String url) {
        CloseableHttpClient httpclient = HttpClients.createDefault();


        //leftTicketDTO.train_date=2018-09-30&leftTicketDTO.from_station=HZH&leftTicketDTO.to_station=VGQ&purpose_codes=ADULT
        try {

            // 创建httpget.
            HttpGet httpget = new HttpGet(url);
            System.out.println("executing request " + httpget.getURI());
            // 执行get请求.
            Header httpHead = new BasicHeader("Cookie", cookies);
            httpget.addHeader(httpHead);
            CloseableHttpResponse response = httpclient.execute(httpget);
            try {
                // 获取响应实体
                HttpEntity entity = response.getEntity();
                // 打印响应状态
                System.out.println(response.getStatusLine());
                if (entity != null) {
                    // 打印响应内容长度
                    System.out.println("Response content length: " + entity.getContentLength());
                    // 打印响应内容
                    System.out.println("Response content: " + EntityUtils.toString(entity));
                }
            } finally {
                response.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭连接,释放资源
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //查询票价
    public static JSONObject queryTrainPrice(String trainNo, String fromIndex, String toIndex, String seatTypes, String departDate) {
        testAvoidS(buildQueryPriceParamStr(trainNo, fromIndex, toIndex, seatTypes, departDate, "https://kyfw.12306.cn/otn/leftTicket/queryTicketPriceFL?"), false);
        return testAvoidS(buildQueryPriceParamStr(trainNo, fromIndex, toIndex, seatTypes, departDate, "https://kyfw.12306.cn/otn/leftTicket/queryTicketPrice?"));
    }

    public static JSONObject testAvoidS(String url) {
        return testAvoidS(url, true);
    }

    public static JSONObject testAvoidS(String url, boolean parseBody) {
        String body = "";

        //采用绕过验证的方式处理https请求
        SSLContext sslcontext = createIgnoreVerifySSL();

        //设置协议http和https对应的处理socket链接工厂的对象
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", new SSLConnectionSocketFactory(sslcontext))
                .build();
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        HttpClients.custom().setConnectionManager(connManager);


        //创建自定义的httpclient对象
        CloseableHttpClient client = HttpClients.custom().setConnectionManager(connManager).build();
        //CloseableHttpClient client = HttpClients.createDefault();

        try {
            //创建get方式请求对象
            HttpGet get = new HttpGet(url);
            Header httpHead = new BasicHeader("Cookie", cookies);
            get.addHeader(httpHead);
            //指定报文头Content-type、User-Agent
            get.setHeader("Content-type", "application/x-www-form-urlencoded");
            get.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:6.0.2) Gecko/20100101 Firefox/6.0.2");
            //执行请求操作，并拿到结果（同步阻塞）
            CloseableHttpResponse response = client.execute(get);
            //获取结果实体
            if (parseBody) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    //按指定编码转换结果实体为String类型
                    body = EntityUtils.toString(entity, "UTF-8");
                }
                EntityUtils.consume(entity);
                //释放链接
                response.close();
                //System.out.println("body:" + body);
                return JSONObject.parseObject(body);
            }
            return null;
        } catch (Exception e) {
            //logger.error("请求异常，Error:{}", ExceptionUtil.getMessage(e));
        } finally {
            try {
                client.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 绕过验证
     *
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    private static SSLContext createIgnoreVerifySSL() {
        try {
            SSLContext sc = SSLContext.getInstance("SSLv3");

            // 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
            X509TrustManager trustManager = new X509TrustManager() {
                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                        String paramString) throws CertificateException {
                }

                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                        String paramString) throws CertificateException {
                }

                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            sc.init(null, new TrustManager[]{trustManager}, null);
            return sc;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject getTrainList(String date, String from, String to) {
        int times = 1;
        JSONObject jsonObject = new JSONObject();
        try {
            while (times++ <= 5) {
                jsonObject = testAvoidS(buildQueryAParamStr(date, from, to));
                if(jsonObject != null){
                    break;
                }
                Thread.sleep(50 * times);

            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return  jsonObject;
    }
}