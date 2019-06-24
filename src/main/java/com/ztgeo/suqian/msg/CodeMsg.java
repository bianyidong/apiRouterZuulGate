package com.ztgeo.suqian.msg;

public enum CodeMsg {

    SUCCESS(200,"请求成功"),
    /******************sdk异常*******************/

    SDK_INTER_ERROR(300,"工具包内部异常"),
    SDK_SIGN_GENERATE_FAIL(301,"签名失败"),
    SDK_SIGN_VERIFY_FAIL(302,"验签失败"),
    SDK_ENCRYPT_FAIL(303,"加密失败"),
    SDK_DECRYPT_FAIL(304,"解密失败"),
    SDK_PARAM_ERROR(305,"参数异常"),

    /******************张宇-过滤器异常*******************/
    API_FILTER_ERROR(501,"无法识别请求接口ID，拒绝访问"),
    AUTHENTICATION_FILTER_ERROR(502,"无法识别身份信息，拒绝访问"),
    IP_FILTER_ERROR(503, "无法识别请求IP，拒绝访问"),
    TIME_FILTER_ERROR(504, "非合法时间请求，拒绝访问"),

    /******************共享平台异常*******************/
    UNRECOGNIZED_IDENTITY(401, "无法识别身份，拒绝访问"),
    ACCESS_DENY(402, "无访问权限"),
    BLACK_USER(403,"对不起，您的IP已被列入黑名单,如需恢复,请联系平台管理人员"),
    NOT_FOUND(404,"无效请求，转发失败"),
    NOT_FOUNDUSER(405,"访问者没有权限，请开放权限"),
    SIGN_ERROR(406,"验签失败"),
    PARAMS_ERROR(407,"参数错误"),
    FAIL(600, "平台网关内部错误");

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
