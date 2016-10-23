package de.mas.wupclient.client.utils;

public class Logger {
    public static void log(String log){
        System.out.println(log);
    }
    public static void logCmd(String log){
        log(">>>" + log);
    }
    public static void logErr(String log){
        System.err.println("> Error: " + log);
    }
}
