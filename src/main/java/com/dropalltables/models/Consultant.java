package com.dropalltables.models;

public class Consultant {
    private int consultantNo;
    private String name;
    private String title;

    public Consultant(int consultantNo, String name, String title) {
        this.consultantNo = consultantNo;
        this.name = name;
        this.title = title;
    }

    public int getConsultantNo() {
        return consultantNo;
    }
    public void setConsultantNo(int consultantNo) {
        this.consultantNo = consultantNo;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "Consultant{" +
                "consultantNo=" + consultantNo +
                ", consultantName='" + name + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
