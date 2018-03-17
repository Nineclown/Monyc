package com.nineclown.monyc.main.chart;

/**
 * Created by nineClown on 2017-11-26.
 */

public class AnalysisBook {
    private String type;
    private String price;
    private String category;
    private String pay;

    public void setType(String type) {
        this.type = type;
    }

    public void setCategory(String category) {
        if ("null".equals(category)) {
            this.category = "기타";
        } else
            this.category = category;
    }


    public void setPrice(String price) {
        this.price = price;
    }

    public void setPay(String pay) {
        if ("null".equals(pay)) {
            this.pay = "기타";
        } else
            this.pay = pay;
    }


    public String getType() {
        return this.type;
    }

    public String getCategory() {
        return this.category;
    }


    public String getPrice() {
        return this.price;
    }

    public String getPay() {
        return this.pay;
    }
}