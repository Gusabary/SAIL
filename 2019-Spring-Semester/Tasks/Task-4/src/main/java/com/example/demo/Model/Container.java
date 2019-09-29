package com.example.demo.Model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Container {

    static private List<Item> container = new ArrayList<Item>();

    //current number of value (auto increment)
    static private int cntValue = 0;

    static final private int threshold = 5;

    static final private int timeout = 30 * 1000;  //30 seconds

    static public List<Integer> getListOfValue() {
        List<Integer> listOfValue = new ArrayList<Integer>();
        container.forEach(item -> {
            listOfValue.add(item.getValue());
        });
        return listOfValue;
    }

    static public List<Integer> produce() {
        cntValue++;
        container.add(new Item(cntValue, new Date()));
        return getListOfValue();
    }

    static public List<Integer> consume() {
        if (container.isEmpty()) {
            return getListOfValue();
        }
        if (container.size() >= threshold) {  //should be a stack now
            container.remove(container.size() - 1);
        }
        else {  //should be a queue now
            container.remove(0);
        }
        return getListOfValue();
    }

    static public void check() {
        while (true) {
            if (container.isEmpty()) {
                break;
            }
            if ((new Date().getTime()) - container.get(0).getDate() < timeout) {
                break;
            }
            container.remove(0);
        }
    }

    static public boolean isEmpty() {
        return container.isEmpty();
    }

}
