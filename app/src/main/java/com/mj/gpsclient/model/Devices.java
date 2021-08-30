package com.mj.gpsclient.model;

/**
 * Created by majin on 15/5/27.
 */
public class Devices {
//    IMEI ：终端IMEI码
//    IMSI ：终端IMSI码
//    Name ：终端名称
//    OnTime ：在线时间
//    LineStatus ：在线状态
//    FromDate ：到期时间
//    WarnNo ：手机号码
//    SIMNo ：报警电话
//    UserName ：所属用户组
    private String IMEI ;
    private String IMSI;
    private String Name;
    private String OnTime;
    private String LineStatus;
    private String FromDate;
    private String WarnNo;
    private String SIMNo;
    private String UserName;


    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getIMEI() {
        return IMEI;
    }

    public void setIMEI(String IMEI) {
        this.IMEI = IMEI;
    }

    public String getIMSI() {
        return IMSI;
    }

    public void setIMSI(String IMSI) {
        this.IMSI = IMSI;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getOnTime() {
        return OnTime;
    }

    public void setOnTime(String onTime) {
        OnTime = onTime;
    }

    public String getLineStatus() {
        return LineStatus;
    }

    public void setLineStatus(String lineStatus) {
        LineStatus = lineStatus;
    }

    public String getFromDate() {
        return FromDate;
    }

    public void setFromDate(String fromDate) {
        FromDate = fromDate;
    }

    public String getWarnNo() {
        return WarnNo;
    }

    public void setWarnNo(String warnNo) {
        WarnNo = warnNo;
    }

    public String getSIMNo() {
        return SIMNo;
    }

    public void setSIMNo(String SIMNo) {
        this.SIMNo = SIMNo;
    }





}
