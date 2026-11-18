package com.pda.uhfdemo.entity;

public class BindInfo {

    private String epc;
    private String name;
    private String status;
    private String tid;
    private Long rssi;
    private int scanCount;
    private long boundAtMillis;
    private long lastSeenMillis;

    public BindInfo() {
    }

    public BindInfo(String epc, String name) {
        this.epc = epc;
        this.name = name;
    }

    public BindInfo(String epc, String name, String tid, long boundAtMillis) {
        this(epc, name);
        this.tid = tid;
        this.boundAtMillis = boundAtMillis;
        this.status = "Bound";
    }

    public String getEpc() {
        return epc;
    }

    public void setEpc(String epc) {
        this.epc = epc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public Long getRssi() {
        return rssi;
    }

    public void setRssi(long rssi) {
        this.rssi = rssi;
    }

    public int getScanCount() {
        return scanCount;
    }

    public void incrementScanCount() {
        scanCount++;
    }

    public long getBoundAtMillis() {
        return boundAtMillis;
    }

    public void setBoundAtMillis(long boundAtMillis) {
        this.boundAtMillis = boundAtMillis;
    }

    public long getLastSeenMillis() {
        return lastSeenMillis;
    }

    public void setLastSeenMillis(long lastSeenMillis) {
        this.lastSeenMillis = lastSeenMillis;
    }

    public BindInfo copy() {
        BindInfo copy = new BindInfo(epc, name);
        copy.status = status;
        copy.tid = tid;
        copy.rssi = rssi;
        copy.scanCount = scanCount;
        copy.boundAtMillis = boundAtMillis;
        copy.lastSeenMillis = lastSeenMillis;
        return copy;
    }
}
