package com.txtled.avs.utils;

import android.Manifest;

/**
 * Created by Mr.Quan on 2019/12/9.
 */
public class Constants {
    public static String[] permissions = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION };
    public static String WIFI_NAME = "amlogic";
    public static int REQUEST_CODE_LOCATION_SETTINGS = 100;
    public static int REQUEST_CODE_WIFI_SETTINGS = 200;
    public static int REQUEST_CODE_INTERNET_SETTINGS = 300;
    public static String RB_ID = "rb_id";
    public static String AVS_WIFI_URL = "http://192.168.2.1/wifilist.html";
    public static final String BUNDLE_KEY_EXCEPTION = "exception";
    public static String SERVICE_TYPE = "_ssh._tcp.";
    public static final String DEVICE_SERIAL_NUMBER = "deviceSerialNumber";
    public static final String PRODUCT_ID = "productID";
    public static final String PRODUCT_INSTANCE_ATTRIBUTES = "productInstanceAttributes";
    public static final String ALEXA_ALL_SCOPE = "alexa:all";
    public static final String[] APP_SCOPES= { ALEXA_ALL_SCOPE };
    public static final String RESET_DEVICE = "reset_device";
    public static final String GET_CODE = "get_code";
    public static final String BIND_URL = "https://amazon.com/us/code";
    public static final String WEB_URL = "web_url";
}
