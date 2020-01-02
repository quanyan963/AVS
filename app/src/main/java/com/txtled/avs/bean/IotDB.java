package com.txtled.avs.bean;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.HashMap;

/**
 * Created by Mr.Quan on 2019/12/31.
 */
@DynamoDBTable(tableName = "IOTDB")
public class IotDB {
    private String USERID;
    private HashMap<String, HashMap<String, String>> CertDic;

    @DynamoDBHashKey(attributeName = "USERID")
    public String getUSERID() {
        return USERID;
    }

    public void setUSERID(String USERID) {
        this.USERID = USERID;
    }

    @DynamoDBAttribute(attributeName = "CertDic")
    public HashMap<String, HashMap<String, String>> getCertDic() {
        return CertDic;
    }

    public void setCertDic(HashMap<String, HashMap<String, String>> certDic) {
        CertDic = certDic;
    }
}
