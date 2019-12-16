package com.txtled.avs.utils;

/**
 * Created by KomoriWu
 * on 2017-09-27.
 */

public class BleUtils {
    public static final String BLE_NAME = "AR";
    public static final String BLE_NAME_MAC = "mi";
    public static final String SERVICE = "ffe0";
    public static final String SEMICOLON = ";";
    public static final String HEAD = "A005+";//AT
    public static final String END = "";//\r\n
    public static final String OPEN_CLOSE = "A"; //开关彩灯
    public static final String LIGHT = "B";//亮度调节
    public static final String POWER = "P";//火苗大小
    public static final String POWER_REQ = "K";//火苗大小
    public static final String SPEED = "S";//彩灯速度
//    public static final String BLE_OPEN_CLOSE = "I";
    public static final String SOUND = "J";//音量
    public static final String REQUEST = "DT";//发送命令返回状态
    public static final String REQUEST_REQ = "D";//发送命令返回状态
    public static final String TO_MUSIC = "T";//发送命令返回状态

    public static String getLightSwitch(boolean b) {
        int state = b ? 1 : 0;
        return HEAD + OPEN_CLOSE + Utils.formatHex(state) + END;
    }

//    public static String getLightSwitchAll(Flame flame) {
//        //return HEAD + LIGHT + Utils.formatHex(progress+1) + END;
//        return HEAD + "CO" + "{" + Utils.formatHex(flame.getLightStatue())
//                +Utils.formatHex(flame.getLight()+1)+Utils.formatHex(flame
//                .getPower()+1) + Utils.formatHex(flame.getToMusic())
//                + Utils.formatHex(flame.getSpeed()+1) + "}" + END;
//    }

    public static String getLight(int statue) {
        return HEAD + LIGHT + Utils.formatHex(statue) + END;
    }
}
