package com.kshrd.android_akn.model;

/**
 * Created by MY-PC on 2/2/2016.
 */
public class SpinnerItem {
    private int id;
    private String name;

    public SpinnerItem(){}
    public SpinnerItem(int id, String name){
        this.id = id;
        this.name = name;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}