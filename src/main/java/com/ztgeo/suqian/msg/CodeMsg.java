package com.ztgeo.suqian.msg;

public enum CodeMsg {

    SUCCESS(200,"请求成功"),
    UNRECOGNIZED_IDENTITY(401, "无法识别身份，拒绝访问"),
    ACCESS_DENY(402, "无访问权限"),
    BLACK_USER(403,"对不起，您的IP已被列入黑名单,如需恢复,请联系平台管理人员"),
    NOT_FOUND(404,"无效请求，转发失败"),
    FAIL(500, "平台网关内部错误");

    private CodeMsg(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public int statusCode() {
        return statusCode;
    }

    public String message() {
        return message;
    }

    private int statusCode; // 状态码
    private String message; // 消息

}
