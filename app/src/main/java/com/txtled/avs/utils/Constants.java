package com.txtled.avs.utils;

import android.Manifest;

/**
 * Created by Mr.Quan on 2019/12/9.
 */
public class Constants {
    public static String[] permissions = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION };
    public static String WIFI_NAME = "amlogic";
    public static int REQUEST_CODE_LOCATION_SETTINGS = 100;
    public static int REQUEST_CODE_WIFI_SETTINGS = 200;
    public static String RB_ID = "rb_id";
    public static String AVS_WIFI_URL = "http://192.168.2.1/wifilist.html";
    public static final String BUNDLE_KEY_EXCEPTION = "exception";
    public static String SERVICE_TYPE = "_ssh._tcp.";
    public static final int MIN_CONNECT_PROGRESS_TIME_MS = 1*1000;
    public static final String DEVICE_SERIAL_NUMBER = "deviceSerialNumber";
    public static final String PRODUCT_ID = "productID";
    public static final String PRODUCT_INSTANCE_ATTRIBUTES = "productInstanceAttributes";
    public static final String ALEXA_ALL_SCOPE = "alexa:all";
    public static final String[] APP_SCOPES= { ALEXA_ALL_SCOPE };
}
