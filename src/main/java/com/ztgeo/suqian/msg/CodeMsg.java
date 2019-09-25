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

    /******************共享平台异常*******************/
    UNRECOGNIZED_IDENTITY(401, "无法识别身份，拒绝访问"),
    ACCESS_DENY(402, "无访问权限"),
    BLACK_USER(403,"对不起，您的IP已被列入黑名单,如需恢复,请联系平台管理人员"),
    NOT_FOUND(404,"无效请求，转发失败"),
    NOT_FOUNDUSER(405,"访问者没有权限，请开放权限"),
    SIGN_ERROR(406,"验签失败"),
    PARAMS_ERROR(407,"参数错误"),
    FROMSIGN_ERROR(408,"共享平台请求方验签过滤器内部异常"),
    FROMDATA_ERROR(409,"共享平台请求方解密过滤器内部异常"),
    TOSIGN_ERROR(410,"共享平台请求方重新加签过滤器异常"),
    TODATA_ERROR(411,"共享平台请求方重新加密过滤器异常"),
    GETNULL_ERROR(412,"请求日志过滤器未获取到from_user或者api_id"),
    RSPSIGN_ERROR(413,"共享平台返回验签过滤器内部异常"),
    RSPDATA_ERROR(414,"共享平台返回解密过滤器异常"),
    AGARSPDATA_ERROR(415,"共享平台返回重新加密过滤器异常"),
    AGARSPSIGN_ERROR(416,"共享平台返回重新加签过滤器异常"),
    RECEIVE_EXCEPTION(417,"接收方业务处理错误，待重新发送"),
    ADDSENDBODY_EXCEPTION(418,"请求方日志过滤器异常"),
    FAIL(500, "平台网关内部错误"),

    /******************张宇-过滤器异常*******************/
    API_FILTER_ERROR(501,"无法识别请求接口ID，拒绝访问"),
    AUTHENTICATION_FILTER_ERROR(502,"无法识别身份信息，拒绝访问"),
    IP_FILTER_ERROR(503, "无法识别请求IP，拒绝访问"),
    TIME_FILTER_ERROR(504, "非合法时间请求，拒绝访问"),
    YXLT_DZ_REQ_ERROR(505,"调用宜兴定制请求过滤器异常！"),
    YXLT_DZ_RESP_ERROR(506,"调用宜兴定制响应过滤器异常！"),
    YXLT_DZ_CONTENT_TYPE_METHOD_ERROR(507,"调用宜兴定制过滤器异常！请求方法及请求内容类型错误!"),
    JSON_KEY_VALUE_FILTER_ERROR(508,"JSON响应KEY-VALUE过滤异常！请检查过滤规则！"),
    YXLT_DZ_TOKEN_ERROR(509,"转发不动产请求TOKEN获取失败！"),
    FAILMAXSCOUNT(510,"接口已达最大访问量！"),
    FAILQUILK(511,"请求过于频繁！"),
    NATIONALSHARED_ERROR(512,"转发国家级共享接口异常！"),
    PROVICESHARED_ERROR(513,"转发省级共享接口异常！"),
    CITY_ERROR(514,"转发市级共享接口异常！"),
    NANKANG_ERROR(515,"转发南康接口异常！");


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
