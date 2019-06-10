package com.ztgeo.suqian.entity;


import java.io.Serializable;
import java.util.Date;

/**
 * @author zoupeidong
 * @version 2018-08-27 16:57:29
 * @email 806316372@qq.com
 */
public class ApiUserRel implements Serializable {
    private String Id;
    private String apiId;
    private  String userId;

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getApiId() {
        return apiId;
    }

    public void setApiId(String apiId) {
        this.apiId = apiId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "ApiUserRel{" +
                "Id='" + Id + '\'' +
                ", apiId='" + apiId + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }
}
