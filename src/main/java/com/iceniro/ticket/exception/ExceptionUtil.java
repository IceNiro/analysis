package com.iceniro.ticket.exception;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 从Exception中读取堆栈信息
 * @author gan.jiangwei
 * @since 2018/8/30 0030.
 */
public class ExceptionUtil {

    //堆栈判断前缀
    private static final String PREFIX = "at com.iceniro.ticket";

    public static String getMessage(Exception e) {
        if(e == null){
            return "";
        }
        //获取异常的名字
        String exceptionName = e.getClass().getName();
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String[] errs = sw.toString().split("\\r\\n\\t");
        //返回报错的最深栈
        for (String err : errs) {

            if (err.startsWith(PREFIX)) {
                //截取最后的（类名.java:行数）字符串
                err = err.substring(err.lastIndexOf("("));
                return new StringBuffer(exceptionName).append(" ").append(err).toString();
            }
        }
        return e.getMessage();
    }
}
