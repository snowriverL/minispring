package com.snowriver.spring.model;

import com.snowriver.spring.framework.annotation.SnowAutowired;
import com.snowriver.spring.framework.annotation.SnowController;
import com.snowriver.spring.framework.annotation.SnowRequestMapping;
import com.snowriver.spring.framework.annotation.SnowRequestParam;
import com.snowriver.spring.framework.webmvc.servlet.SnowModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 公布接口url
 * @author Tom
 *
 */
@SnowController
@SnowRequestMapping("/web")
public class MyAction {

    @SnowAutowired
    IQueryService queryService;
    @SnowAutowired
    IModifyService modifyService;

    @SnowRequestMapping("/query.json")
    public SnowModelAndView query(HttpServletRequest request, HttpServletResponse response,
                                  @SnowRequestParam("name") String name){
        String result = queryService.query(name);
        return out(response,result);
    }

    @SnowRequestMapping("/add*.json")
    public SnowModelAndView add(HttpServletRequest request,HttpServletResponse response,
                              @SnowRequestParam("name") String name,@SnowRequestParam("addr") String addr){
        String result = null;
        try {
            result = modifyService.add(name,addr);
            return out(response,result);
        } catch (Exception e) {
//			e.printStackTrace();
            Map<String,Object> model = new HashMap<String,Object>();
            model.put("detail",e.getCause().getMessage());
//			System.out.println(Arrays.toString(e.getStackTrace()).replaceAll("\\[|\\]",""));
            model.put("stackTrace", Arrays.toString(e.getStackTrace()).replaceAll("\\[|\\]",""));
            return new SnowModelAndView("500",model);
        }

    }

    @SnowRequestMapping("/remove.json")
    public SnowModelAndView remove(HttpServletRequest request,HttpServletResponse response,
                                 @SnowRequestParam("id") Integer id){
        String result = modifyService.remove(id);
        return out(response,result);
    }

    @SnowRequestMapping("/edit.json")
    public SnowModelAndView edit(HttpServletRequest request,HttpServletResponse response,
                               @SnowRequestParam("id") Integer id,
                               @SnowRequestParam("name") String name){
        String result = modifyService.edit(id,name);
        return out(response,result);
    }



    private SnowModelAndView out(HttpServletResponse resp,String str){
        try {
            resp.getWriter().write(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
