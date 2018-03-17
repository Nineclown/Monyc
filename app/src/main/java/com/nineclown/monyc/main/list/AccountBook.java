package com.nineclown.monyc.main.list;

import java.text.DecimalFormat;

/**
 * Created by nineClown on 2017-11-18.
 */

public class AccountBook {
    private String id;
    private String type;
    private String price;
    private String category;
    private String pay;
    private String contents;
    private String date;

    public void setId(String id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCategory(String category) {
        if ("null".equals(category)) {
            if ("expenses".equals(type)) {
                this.category = "지출";
            } else if ("income".equals(type)) {
                this.category = "수입";
            }
        } else
            this.category = category;
    }

    public void setContents(String contents) {
        if ("null".equals(contents)) {
            this.contents = "";
        } else
            this.contents = contents;
    }

    public void setPrice(String price) {
        long value = Long.parseLong(price);
        DecimalFormat format = new DecimalFormat("###,###원");
        this.price = format.format(value);
    }

    public void setPay(String pay) {
        if ("null".equals(pay)) {
            this.pay = "";
        } else
            this.pay = pay;
    }

    public void setDate(String date) {
        this.date = date;
    }

    /*
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public Drawable getType() {
        if ("expenses".equals(type)) {
            return context.getDrawable(R.drawable.main_date_bg_b);
        } else if ("income".equals(type)) {
            return context.getDrawable(R.drawable.main_date_bg_r);
        } else {
            return context.getDrawable(R.drawable.main_date_bg_r);
        }
    }
    */

    public String getId() {
        return this.id;
    }

    public String getType() {
        return this.type;
    }

    public String getCategory() {
        return this.category;
    }

    public String getContents() {
        return this.contents;
    }

    public String getPrice() {
        return this.price;
    }

    public String getPay() {
        return this.pay;
    }

    public String getDate() {
        return this.date;
    }
}
