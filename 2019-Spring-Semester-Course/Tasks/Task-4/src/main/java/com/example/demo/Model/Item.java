package com.example.demo.Model;

import java.util.Date;

public class Item {

    private int value;

    private Date insertAt;

    public Item(int value, Date insertAt) {
        this.value = value;
        this.insertAt = insertAt;
    }

    public int getValue() {
        return value;
    }

    public long getDate() {
        return insertAt.getTime();
    }

}
