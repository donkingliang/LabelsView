package com.donkingliang.labelsviewdemo;

/**
 * Depiction:
 * Author:lry
 * Date:2018/1/20
 */

public class TestBean {

    private String name;
    private int id;

    public TestBean(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "TestBean{" +
                "name='" + name + '\'' +
                ", id=" + id +
                '}';
    }
}
