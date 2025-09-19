package com.dropalltables.models;

public class Consultant {
    private int consultantId;
    private int consultantNo;
    private String consultantName;
    private String title;

    public Consultant(int consultantId, int consultantNo, String consultantName, String title) {
        this.consultantId = consultantId;
        this.consultantNo = consultantNo;
        this.consultantName = consultantName;
        this.title = title;
    }

    public int getConsultantId() {
        return consultantId;
    }
    public void setConsultantId(int consultantId) {
        this.consultantId = consultantId;
    }

    public int getConsultantNo() {
        return consultantNo;
    }
    public void setConsultantNo(int consultantNo) {
        this.consultantNo = consultantNo;
    }

    public String getConsultantName() {
        return consultantName;
    }
    public void setConsultantName(String consultantName) {
        this.consultantName = consultantName;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String toString() {
        return consultantName;
    }
}
