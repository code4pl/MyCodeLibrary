package com.pl.app.model;

/**
 * Created by Alex on 2016/7/22.
 */
public class Department {
    private String name;
    private String manager;

    public Department(String name, String manager) {
        this.name = name;
        this.manager = manager;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }
}
