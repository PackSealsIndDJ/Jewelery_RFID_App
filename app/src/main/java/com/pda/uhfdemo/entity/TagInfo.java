package com.pda.uhfdemo.entity;

import java.io.Serializable;
import java.util.Date;

public class TagInfo implements Serializable {
    private Integer index;
    private String type;
    private String epc;
    private String encodedData;
    private String encodedDataBank;
    private String encodedReadError;
    private String tid;
    private String rssi;
    private String userData;
    private String reserved;

    private boolean errorTag;
    private Date readTime;
    private double ctesius;
    private Integer count;
    private int scanCount;
    private long firstScanTime;
    private long lastScanTime;
    private boolean encodedReadSuccess;
    private int encodedReadSuccessCount;
    private int encodedReadFailureCount;


    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEpc() {
        return epc;
    }

    public void setEpc(String epc) {
        this.epc = epc;
    }

    public String getEncodedData() {
        return encodedData;
    }

    public void setEncodedData(String encodedData) {
        this.encodedData = encodedData;
    }

    public String getEncodedDataBank() {
        return encodedDataBank;
    }

    public void setEncodedDataBank(String encodedDataBank) {
        this.encodedDataBank = encodedDataBank;
    }

    public String getEncodedReadError() {
        return encodedReadError;
    }

    public void setEncodedReadError(String encodedReadError) {
        this.encodedReadError = encodedReadError;
    }

    public boolean isEncodedReadSuccess() {
        return encodedReadSuccess;
    }

    public void setEncodedReadSuccess(boolean encodedReadSuccess) {
        this.encodedReadSuccess = encodedReadSuccess;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getRssi() {
        return rssi;
    }

    public void setRssi(String rssi) {
        this.rssi = rssi;
    }

    public String getUserData() {
        return userData;
    }

    public void setUserData(String userData) {
        this.userData = userData;
    }

    public String getReserved() {
        return reserved;
    }

    public void setReserved(String reserved) {
        this.reserved = reserved;
    }

    public Date getReadTime() {
        return readTime;
    }

    public void setReadTime(Date readTime) {
        this.readTime = readTime;
    }

    public double getCtesius() {
        return ctesius;
    }

    public void setCtesius(double ctesius) {
        this.ctesius = ctesius;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public int getScanCount() {
        return scanCount;
    }

    public void setScanCount(int scanCount) {
        this.scanCount = scanCount;
    }

    public long getFirstScanTime() {
        return firstScanTime;
    }

    public void setFirstScanTime(long firstScanTime) {
        this.firstScanTime = firstScanTime;
    }

    public long getLastScanTime() {
        return lastScanTime;
    }

    public void setLastScanTime(long lastScanTime) {
        this.lastScanTime = lastScanTime;
    }

    public int getEncodedReadSuccessCount() {
        return encodedReadSuccessCount;
    }

    public int getEncodedReadFailureCount() {
        return encodedReadFailureCount;
    }

    public void recordScan(TagInfo observed, long scannedAt) {
        scanCount++;
        count = scanCount;
        if (firstScanTime == 0) {
            firstScanTime = scannedAt;
        }
        lastScanTime = scannedAt;
        readTime = new Date(scannedAt);
        if (observed != null) {
            rssi = observed.rssi;
            if (observed.encodedReadSuccess) {
                encodedReadSuccessCount++;
                encodedData = observed.encodedData;
                encodedDataBank = observed.encodedDataBank;
                encodedReadSuccess = true;
                encodedReadError = null;
            } else {
                encodedReadFailureCount++;
                if (!encodedReadSuccess) {
                    encodedReadError = observed.encodedReadError;
                }
            }
        }
    }

    public TagInfo copy() {
        TagInfo copy = new TagInfo();
        copy.index = index;
        copy.type = type;
        copy.epc = epc;
        copy.encodedData = encodedData;
        copy.encodedDataBank = encodedDataBank;
        copy.encodedReadError = encodedReadError;
        copy.tid = tid;
        copy.rssi = rssi;
        copy.userData = userData;
        copy.reserved = reserved;
        copy.errorTag = errorTag;
        copy.readTime = readTime == null ? null : new Date(readTime.getTime());
        copy.ctesius = ctesius;
        copy.count = count;
        copy.scanCount = scanCount;
        copy.firstScanTime = firstScanTime;
        copy.lastScanTime = lastScanTime;
        copy.encodedReadSuccess = encodedReadSuccess;
        copy.encodedReadSuccessCount = encodedReadSuccessCount;
        copy.encodedReadFailureCount = encodedReadFailureCount;
        return copy;
    }

    public boolean isErrorTag() {
        return errorTag;
    }

    public void setErrorTag(boolean errorTag) {
        this.errorTag = errorTag;
    }
}
