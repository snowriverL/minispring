package com.snowriver.spring.model;

import com.snowriver.spring.framework.annotation.SnowService;

@SnowService
public class SnowServiceImpl implements ISnowService {


    public String getName(String name) {
        return "这山长水远的人世，终究要一个人走过----------------------/r/n  -----------： " + name;
    }
}