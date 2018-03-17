package com.nineclown.monyc.main.chart;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by nineClown on 2017-11-26.
 */

public class AnalysisMap {
    private static final String TAG = "AnalysisMap";

    private ArrayList<String> category;
    private ArrayList<Float> price;
    private ArrayList<Integer> count;

    public AnalysisMap() {
        category = new ArrayList<>();
        price = new ArrayList<>();
        count = new ArrayList<>();
    }

    public void add(String category, String price) {
        Log.d(TAG, "add: parameter is " + category + "price : " + price);
        if (chkItem(category)) {
            Float result = this.price.get(this.category.indexOf(category)) + Float.parseFloat(price);
            this.price.set(this.category.indexOf(category), result);
            // Log.d(TAG, "add: category index" + this.category.indexOf(category) + "price" + result);
            int result2 = count.get(this.category.indexOf(category)) + 1;
            count.add(this.category.indexOf(category), result2);
        } else {
            this.category.add(category);
            this.price.add(this.category.indexOf(category), Float.parseFloat(price));
            // Log.d(TAG, "add: first) category index" + this.category.indexOf(category) + "price" + price);
            count.add(this.category.indexOf(category), 1);
        }
    }

    public int size() {
        return price.size();
    }

    private boolean chkItem(String item) {
        return category.contains(item);
    }

    public Float getPrice(int count) {
        return price.get(count);
    }

    public String getCategory(int i) {
        return category.get(i);
    }

    public int getCount(int i) {
        return count.get(i);
    }

    public void clear() {
        category.clear();
        price.clear();
        count.clear();
    }
}

