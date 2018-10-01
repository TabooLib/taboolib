package com.ilummc.eagletdl;

/**
 * Test class
 */
public class Eaglet {

    public static void main(String[] args) {
        //new EagletTask().url("http://sgp-ping.vultr.com/vultr.com.100MB.bin")
        new EagletTask().url("https://gitee.com/bkm016/TabooLibCloud/raw/master/TabooMenu/TabooMenu.jar")
                .file("F:\\test.dl")
                .setThreads(1)
                .readTimeout(1000)
                .connectionTimeout(1000)
                .maxRetry(30)
                .setOnConnected(event -> System.out.println(event.getContentLength()))
                .setOnProgress(event -> System.out.println(event.getSpeedFormatted() + " " + event.getPercentageFormatted()))
                .setOnComplete(event -> System.out.println("Complete"))
                .start();
    }

}
