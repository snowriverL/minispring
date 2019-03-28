package com.snowriver.spring.model;

import com.snowriver.spring.annotation.SnowAutowired;
import com.snowriver.spring.annotation.SnowController;
import com.snowriver.spring.annotation.SnowRequestParam;
import com.snowriver.spring.annotation.SnowRequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@SnowController
@SnowRequestMapping("/snow")
public class SnowRiverController {

    @SnowAutowired
    private ISnowService snowService;

    @SnowRequestMapping("/query")
    public void query(HttpServletRequest req, HttpServletResponse resp,
                      @SnowRequestParam("name") String name){
        String result = snowService.getName(name);
        try {
            resp.getWriter().write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @SnowRequestMapping("/add")
    public void add(HttpServletRequest req, HttpServletResponse resp,
                    @SnowRequestParam("a") Integer a, @SnowRequestParam("b") Integer b){
        try {
            resp.getWriter().write(a + "+" + b + "=" + (a + b));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @SnowRequestMapping("/remove")
    public void remove(HttpServletRequest req,HttpServletResponse resp,
                       @SnowRequestParam("id") Integer id){
        try {
            resp.getWriter().write("======================" + id);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}