package com.ztgeo.suqian.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import org.dom4j.*;
import org.springframework.util.StringUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class XmlAndJsonUtils {

    public static String json2xml(String jsonStr) {
        try {
            StringBuffer buffer = new StringBuffer();
            buffer.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
            JSONObject jObj = JSON.parseObject(jsonStr);
            jsonToXmlstr(jObj, buffer);
            return buffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";

        }
    }

    public static String json2xml_UpperCase(String jsonStr) {
        try {
            StringBuffer buffer = new StringBuffer();
            buffer.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
            JSONObject jObj = JSONObject.parseObject(jsonStr);
            jsonToXmlstr_UpperCase(jObj, buffer);
            return buffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";

        }
    }

    private static String jsonToXmlstr(JSONObject jObj, StringBuffer buffer) {
        Set<Map.Entry<String, Object>> se = jObj.entrySet();
        for (Iterator<Map.Entry<String, Object>> it = se.iterator(); it.hasNext(); ) {
            Map.Entry<String, Object> en = it.next();
            if (en.getValue().getClass().getName().equals("com.alibaba.fastjson.JSONObject")) {
                buffer.append("<" + en.getKey() + ">");
                JSONObject jo = jObj.getJSONObject(en.getKey());
                jsonToXmlstr(jo, buffer);
                buffer.append("</" + en.getKey() + ">");
            } else if (en.getValue().getClass().getName().equals("com.alibaba.fastjson.JSONArray")) {
                JSONArray jarray = jObj.getJSONArray(en.getKey());
                for (int i = 0; i < jarray.size(); i++) {
                    buffer.append("<" + en.getKey() + ">");
                    JSONObject jsonobject = jarray.getJSONObject(i);
                    jsonToXmlstr(jsonobject, buffer);
                    buffer.append("</" + en.getKey() + ">");
                }
            } else if (en.getValue().getClass().getName().equals("java.lang.String")) {
                buffer.append("<" + en.getKey() + ">" + en.getValue());
                buffer.append("</" + en.getKey() + ">");
            }
        }
        return buffer.toString();
    }

    private static String jsonToXmlstr_UpperCase(JSONObject jObj, StringBuffer buffer) {
        Set<Map.Entry<String, Object>> se = jObj.entrySet();
        for (Iterator<Map.Entry<String, Object>> it = se.iterator(); it.hasNext(); ) {
            Map.Entry<String, Object> en = it.next();
                if (en.getValue().getClass().getName().equals("com.alibaba.fastjson.JSONObject")) {
                    buffer.append("<" + en.getKey() + ">");
                    JSONObject jo = jObj.getJSONObject(en.getKey());
                    jsonToXmlstr_UpperCase(jo, buffer);
                    buffer.append("</" + en.getKey() + ">");
                } else if (en.getValue().getClass().getName().equals("com.alibaba.fastjson.JSONArray")) {
                    JSONArray jarray = jObj.getJSONArray(en.getKey());
                    for (int i = 0; i < jarray.size(); i++) {
                        buffer.append("<" + en.getKey() + ">");
                        JSONObject jsonobject = jarray.getJSONObject(i);
                        jsonToXmlstr_UpperCase(jsonobject, buffer);
                        buffer.append("</" + en.getKey() + ">");
                    }
                } else if (en.getValue().getClass().getName().equals("java.lang.String")) {
                    buffer.append("<" + en.getKey() + ">" + en.getValue());
                    buffer.append("</" + en.getKey() + ">");
                }
            }

        return buffer.toString();
    }


    public static JSONObject xml2json(String xmlStr) {
        try {
            Document doc = DocumentHelper.parseText(xmlStr);
            JSONObject json = new JSONObject();
            Element ele = doc.getRootElement();
            dom4j2Json(ele, json);
            return json;
        } catch (DocumentException e) {
            e.printStackTrace();
            throw new RuntimeException("xml文件转换异常！");
        }
    }

    private static void dom4j2Json(Element element, JSONObject json) {

        //如果是属性
        for (Object o : element.attributes()) {
            Attribute attr = (Attribute) o;
            if (!isEmpty(attr.getValue())) {
                json.put("@" + attr.getName(), attr.getValue());
            }
        }
        List<Element> chdEl = element.elements();
        if (chdEl.isEmpty() && !isEmpty(element.getText())) {//如果没有子元素,只有一个值
            //System.out.println("dddddddd");
            json.put(element.getName(), element.getText());
        }

        for (Element e : chdEl) {//有子元素
            if (!e.elements().isEmpty()) {//子元素也有子元素
                JSONObject chdjson = new JSONObject();
                dom4j2Json(e, chdjson);
                Object o = json.get(e.getName());
                if (o != null) {
                    JSONArray jsona = null;
                    if (o instanceof JSONObject) {//如果此元素已存在,则转为jsonArray
                        JSONObject jsono = (JSONObject) o;
                        json.remove(e.getName());
                        jsona = new JSONArray();
                        jsona.add(jsono);
                        jsona.add(chdjson);
                    }
                    if (o instanceof JSONArray) {
                        jsona = (JSONArray) o;
                        jsona.add(chdjson);
                    }
                    json.put(e.getName(), jsona);
                } else {
                    if (!chdjson.isEmpty()) {
                        json.put(e.getName(), chdjson);
                    }
                }


            } else {//子元素没有子元素
                for (Object o : element.attributes()) {
                    Attribute attr = (Attribute) o;
                    if (!isEmpty(attr.getValue())) {
                        json.put("@" + attr.getName(), attr.getValue());
                    }
                }
                if (!e.getText().isEmpty()) {
                    json.put(e.getName(), e.getText());
                }
            }
        }

    }

    private static boolean isEmpty(String str) {

        if (str == null || str.trim().isEmpty() || "null".equals(str)) {
            return true;
        }
        return false;
    }
}
