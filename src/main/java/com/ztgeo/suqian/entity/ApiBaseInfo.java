package com.ztgeo.suqian.entity;


import java.io.Serializable;
import java.util.Date;

/**
 * @author zoupeidong
 * @version 2018-08-27 16:57:29
 * @email 806316372@qq.com
 */
public class ApiBaseInfo implements Serializable {

    private String apiId;

    private String apiPubkey;

    //api名称
    private String apiName;

    //基础URL
    private String baseUrl;

    //后缀
    private String path;

    //接口method
    private String method;

    //启用状态,0启用,1未启用
    private Boolean enabledStatus;

    //所属机构ID
    private String apiOwnerId;

    //所属机构名称
    private String apiOwnerName;

    //创建者ID
    private String crtUserId;

    //创建时间
    private Date crtTime;

    //修改者ID
    private String updUserId;

    //修改时间
    private Date updTime;
    //api类型0位安全接口1为通用接口
    private int apiType;

    /**
     * 设置：主键ID
     */
    public void setApiId(String apiId) {
        this.apiId = apiId;
    }

    /**
     * 获取：主键ID
     */
    public String getApiId() {
        return apiId;
    }

    /**
     * 设置：公开的服务ID
     */
    public void setApiPubkey(String apiPubkey) {
        this.apiPubkey = apiPubkey;
    }

    /**
     * 获取：公开的服务ID
     */
    public String getApiPubkey() {
        return apiPubkey;
    }

    /**
     * 设置：api名称
     */
    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    /**
     * 获取：api名称
     */
    public String getApiName() {
        return apiName;
    }

    /**
     * 设置：基础URL
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * 获取：基础URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * 设置：后缀
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 获取：后缀
     */
    public String getPath() {
        return path;
    }

    /**
     * 设置：接口method
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * 获取：接口method
     */
    public String getMethod() {
        return method;
    }

    /**
     * 设置：启用状态,0启用,1未启用
     */
    public void setEnabledStatus(Boolean enabledStatus) {
        this.enabledStatus = enabledStatus;
    }

    /**
     * 获取：启用状态,0启用,1未启用
     */
    public Boolean getEnabledStatus() {
        return enabledStatus;
    }

    /**
     * 设置：所属机构ID
     */
    public void setApiOwnerId(String apiOwnerId) {
        this.apiOwnerId = apiOwnerId;
    }

    /**
     * 获取：所属机构ID
     */
    public String getApiOwnerId() {
        return apiOwnerId;
    }

    /**
     * 设置：所属机构名称
     */
    public void setApiOwnerName(String apiOwnerName) {
        this.apiOwnerName = apiOwnerName;
    }

    /**
     * 获取：所属机构名称
     */
    public String getApiOwnerName() {
        return apiOwnerName;
    }

    /**
     * 设置：创建者ID
     */
    public void setCrtUserId(String crtUserId) {
        this.crtUserId = crtUserId;
    }

    /**
     * 获取：创建者ID
     */
    public String getCrtUserId() {
        return crtUserId;
    }

    public Date getCrtTime() {
        return crtTime;
    }

    public void setCrtTime(Date crtTime) {
        this.crtTime = crtTime;
    }

    public Date getUpdTime() {
        return updTime;
    }

    public void setUpdTime(Date updTime) {
        this.updTime = updTime;
    }

    /**
     * 设置：修改者ID
     */
    public void setUpdUserId(String updUserId) {
        this.updUserId = updUserId;
    }

    /**
     * 获取：修改者ID
     */
    public String getUpdUserId() {
        return updUserId;
    }

    /**
     *获取：api类型
     */
    public int getApiType() {
        return apiType;
    }

    public void setApiType(int apiType) {
        this.apiType = apiType;
    }

    @Override
    public String toString() {
        return "ApiBaseInfo{" +
                "apiId='" + apiId + '\'' +
                ", apiPubkey='" + apiPubkey + '\'' +
                ", apiName='" + apiName + '\'' +
                ", baseUrl='" + baseUrl + '\'' +
                ", path='" + path + '\'' +
                ", method='" + method + '\'' +
                ", enabledStatus=" + enabledStatus +
                ", apiOwnerId='" + apiOwnerId + '\'' +
                ", apiOwnerName='" + apiOwnerName + '\'' +
                ", crtUserId='" + crtUserId + '\'' +
                ", crtTime=" + crtTime +
                ", updUserId='" + updUserId + '\'' +
                ", updTime=" + updTime +
                ", apiType='" + apiType + '\'' +
                '}';
    }
}
