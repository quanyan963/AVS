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
    public static final String IDENTITY_POOL_ID = "us-east-1:a7fd6e3b-f444-41e7-ae9b-38f8ccef53cb";
    public static final String GET_CODE = "get_code";
    public static final String BIND_URL = "https://www.amazon.com/ap/signin?openid.return_to=https%3A%2F%2Fwww.amazon.com%2Fa%2Fap-post-redirect%3FsiteState%3DclientContext%253D140-1435310-7455029%252CsourceUrl%253Dhttps%25253A%25252F%25252Fwww.amazon.com%25252Fa%25252Fcode%25253Flanguage%25253Den_US%252Csignature%253DxhW9zj2FRVpsXUPhmrRFm9o4b0E3Aj3D&openid.identity=http%3A%2F%2Fspecs.openid.net%2Fauth%2F2.0%2Fidentifier_select&openid.assoc_handle=amzn_chimera_code_based_linking&openid.mode=checkid_setup&marketPlaceId=ATVPDKIKX0DEC&language=en_US&openid.claimed_id=http%3A%2F%2Fspecs.openid.net%2Fauth%2F2.0%2Fidentifier_select&pageId=amzn_chimera_code_based_linking&openid.ns=http%3A%2F%2Fspecs.openid.net%2Fauth%2F2.0&openid.pape.max_auth_age=10";//https://amazon.com/us/code
    public static final String WEB_URL = "web_url";
    public static final int SOCKET_UDP_PORT = 9001;
    public static final String DISCOVERY = "{\"discovery\":1}";
    public static final String DB_NAME = "IOTDB";
    public static final String USER_ID = "USERID";
    public static final String THING_DIR = "ThingDir";
    public static final String MY_OIT_CE = "myiotce";
    public static final String SEND_THING_NAME = "{\"endpoint\":\"%s\",\"thing\":\"%s\"}";
    public static final String SEND_CA_ONE = "{\"ca0\":\"%s\"}";
    public static final String SEND_CA_TWO = "{\"ca1\":\"%s\"}";
    public static final String SEND_CERT_ONE = "{\"cert0\":\"%s\"}";
    public static final String SEND_CERT_TWO = "{\"cert1\":\"%s\"}";
    public static final String SEND_KEY_ONE = "{\"key0\":\"%s\"}";
    public static final String SEND_KEY_TWO = "{\"key1\":\"%s\"}";
    public static final String REBOOT = "{\"reboot\":1}";
    public static final String FRIENDLY_NAME = "{\"friendlyname\":\"%s\"}";
    public static final String REST_API = "a311cdvk7hqtsk-ats.iot.us-east-1.amazonaws.com";
    public static final String POLICY_JSON = "{\n" +
            "  \"Version\": \"2012-10-17\",\n" +
            "  \"Statement\": [\n" +
            "    {\n" +
            "      \"Effect\": \"Allow\",\n" +
            "      \"Action\": \"iot:*\",\n" +
            "      \"Resource\": \"*\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";
    public static final String CA = "-----BEGIN CERTIFICATE-----\n" +
            "MIIDQTCCAimgAwIBAgITBmyfz5m/jAo54vB4ikPmljZbyjANBgkqhkiG9w0BAQsF\n" +
            "ADA5MQswCQYDVQQGEwJVUzEPMA0GA1UEChMGQW1hem9uMRkwFwYDVQQDExBBbWF6\n" +
            "b24gUm9vdCBDQSAxMB4XDTE1MDUyNjAwMDAwMFoXDTM4MDExNzAwMDAwMFowOTEL\n" +
            "MAkGA1UEBhMCVVMxDzANBgNVBAoTBkFtYXpvbjEZMBcGA1UEAxMQQW1hem9uIFJv\n" +
            "b3QgQ0EgMTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBALJ4gHHKeNXj\n" +
            "ca9HgFB0fW7Y14h29Jlo91ghYPl0hAEvrAIthtOgQ3pOsqTQNroBvo3bSMgHFzZM\n" +
            "9O6II8c+6zf1tRn4SWiw3te5djgdYZ6k/oI2peVKVuRF4fn9tBb6dNqcmzU5L/qw\n" +
            "IFAGbHrQgLKm+a/sRxmPUDgH3KKHOVj4utWp+UhnMJbulHheb4mjUcAwhmahRWa6\n" +
            "VOujw5H5SNz/0egwLX0tdHA114gk957EWW67c4cX8jJGKLhD+rcdqsq08p8kDi1L\n" +
            "93FcXmn/6pUCyziKrlA4b9v7LWIbxcceVOF34GfID5yHI9Y/QCB/IIDEgEw+OyQm\n" +
            "jgSubJrIqg0CAwEAAaNCMEAwDwYDVR0TAQH/BAUwAwEB/zAOBgNVHQ8BAf8EBAMC\n" +
            "AYYwHQYDVR0OBBYEFIQYzIU07LwMlJQuCFmcx7IQTgoIMA0GCSqGSIb3DQEBCwUA\n" +
            "A4IBAQCY8jdaQZChGsV2USggNiMOruYou6r4lK5IpDB/G/wkjUu0yKGX9rbxenDI\n" +
            "U5PMCCjjmCXPI6T53iHTfIUJrU6adTrCC2qJeHZERxhlbI1Bjjt/msv0tadQ1wUs\n" +
            "N+gDS63pYaACbvXy8MWy7Vu33PqUXHeeE6V/Uq2V8viTO96LXFvKWlJbYK8U90vv\n" +
            "o/ufQJVtMVT8QtPHRh8jrdkPSHCa2XV4cdFyQzR1bldZwgJcJmApzyMZFo6IQ6XU\n" +
            "5MsI+yMRQ+hDKXJioaldXgjUkK642M4UwtBV8ob2xJNDd2ZhwLnoQdeXeGADbkpy\n" +
            "rqXRfboQnoZsG4q5WTP468SQvvG5\n" +
            "-----END CERTIFICATE-----\n";
}
