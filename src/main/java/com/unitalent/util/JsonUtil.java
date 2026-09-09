package com.unitalent.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JsonUtil {

    public static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

    /** Lee el cuerpo JSON del request y lo devuelve como Map<String,Object> */
    public static Map<String, Object> readBody(HttpServletRequest req) throws IOException {
        req.setCharacterEncoding("UTF-8");
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        if (sb.length() == 0)
            return new HashMap<>();
        Map<String, Object> map = GSON.fromJson(sb.toString(), Map.class);
        return map != null ? map : new HashMap<>();
    }

    public static void sendJson(HttpServletResponse resp, int status, Object data) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(GSON.toJson(data));
    }

    public static void sendError(HttpServletResponse resp, int status, String message) throws IOException {
        Map<String, Object> err = new HashMap<>();
        err.put("ok", false);
        err.put("error", message);
        sendJson(resp, status, err);
    }
}
