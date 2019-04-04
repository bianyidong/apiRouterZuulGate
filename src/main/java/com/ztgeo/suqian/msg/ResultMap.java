package com.ztgeo.suqian.msg;

import com.alibaba.fastjson.JSONObject;

import java.util.Objects;

/**
 * 统一返回消息格式类
 *
 * @author zoupeidong
 * @see CodeMsg
 */
public class ResultMap {

    private int statusCode; // 状态码
    private String message; // 消息
    private String errorCause; // 错误原因

    private static JSONObject jsonObj = new JSONObject();

    public ResultMap(CodeMsg codeMsg) {
        this.statusCode = codeMsg.statusCode();
        this.message = codeMsg.message();
        this.errorCause = "";
    }

    public ResultMap(CodeMsg codeMsg,String errorCause) {
        this.statusCode = codeMsg.statusCode();
        this.message = codeMsg.message();
        this.errorCause = errorCause;
    }

    public ResultMap(int statusCode, String message, String errorCause) {
        this.statusCode = statusCode;
        this.message = message;
        this.errorCause = errorCause;
    }

    public static ResultMap ok(){
        return ResultMap.ok(CodeMsg.SUCCESS);
    }

    public static ResultMap ok(CodeMsg codeMsg){
        return new ResultMap(codeMsg);
    }

    public static ResultMap error(CodeMsg codeMsg){
        return new ResultMap(codeMsg);
    }

    public static ResultMap error(int statusCode, String message, String errorCause){
        return new ResultMap(statusCode,message,errorCause);
    }

    public static ResultMap error(CodeMsg codeMsg,String errorCause){
        return new ResultMap(codeMsg,errorCause);
    }

    @Override
    public String toString() {
        jsonObj.clear();
        jsonObj.put("code",statusCode);
        jsonObj.put("message", message);
        if(!Objects.equals(null,errorCause) && !Objects.equals("",errorCause)){
            jsonObj.put("errorCause",errorCause);
        }
        return jsonObj.toJSONString();
    }

}
