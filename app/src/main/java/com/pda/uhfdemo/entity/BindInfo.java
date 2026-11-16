package com.pda.uhfdemo.entity;

public class BindInfo {

    private String epc;
    private String name;
    private String status;

    public BindInfo() {
    }

    public BindInfo(String epc, String name) {
        this.epc = epc;
        this.name = name;
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
}
