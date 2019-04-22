package com.snowriver.spring.framework.webmvc.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SnowView {

    public final String DEFULAT_CONTENT_TYPE = "text/html;charset=utf-8";

    private File viewFile;

    public SnowView(File viewFile) {
        this.viewFile = viewFile;
    }

    public String getContentType() {
        return DEFULAT_CONTENT_TYPE;
    }

    public void render(Map<String,?> model, HttpServletRequest request, HttpServletResponse response) throws Exception{
        StringBuffer sb = new StringBuffer();
        RandomAccessFile ra = new RandomAccessFile(this.viewFile, "r");

        try {
            String line = null;
            while (null != (line = ra.readLine())) {
                line = new String(line.getBytes("ISO-8859-1"),"utf-8");
                Pattern pattern = Pattern.compile("￥\\{[^\\}]+\\}",Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(line);

                while (matcher.find()) {
                    String paranName = matcher.group();
                    paranName = paranName.replaceAll("￥\\{|\\}", "");
                    Object paramValue = model.get(paranName);
                    if (null == paramValue) {
                        continue;
                    }

                    //要把￥{}中间的这个字符串给取出来
                    line = matcher.replaceFirst(makeStringForRegExp(paramValue.toString()));
                    matcher = pattern.matcher(line);
                }

                sb.append(line);

            }
        } finally {
            ra.close();
        }

        response.setCharacterEncoding("utf-8");
        response.getWriter().write(sb.toString());
    }

    private String makeStringForRegExp(String str) {
        return str.replace("\\", "\\\\").replace("*", "\\*")
                .replace("+", "\\+").replace("|", "\\|")
                .replace("{", "\\{").replace("}", "\\}")
                .replace("(", "\\(").replace(")", "\\)")
                .replace("^", "\\^").replace("$", "\\$")
                .replace("[", "\\[").replace("]", "\\]")
                .replace("?", "\\?").replace(",", "\\,")
                .replace(".", "\\.").replace("&", "\\&");
    }
}