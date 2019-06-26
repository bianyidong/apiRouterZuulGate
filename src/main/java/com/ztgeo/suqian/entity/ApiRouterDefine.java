package com.ztgeo.suqian.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class ApiRouterDefine implements Serializable {

    private String id;
    private String path;
    private String service_id;
    private String url;
    private Integer retryable;
    private Integer enabled;
    private Integer strip_prefix;
    private String crt_user_name;
    private String crt_user_id;
    private Date crt_time;
    private String upd_user_name;
    private String upd_user_id;
    private Date upd_time;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getService_id() {
        return service_id;
    }

    public void setService_id(String service_id) {
        this.service_id = service_id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getRetryable() {
        return retryable;
    }

    public void setRetryable(Integer retryable) {
        this.retryable = retryable;
    }

    public Integer getEnabled() {
        return enabled;
    }

    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }

    public Integer getStrip_prefix() {
        return strip_prefix;
    }

    public void setStrip_prefix(Integer strip_prefix) {
        this.strip_prefix = strip_prefix;
    }

    public String getCrt_user_name() {
        return crt_user_name;
    }

    public void setCrt_user_name(String crt_user_name) {
        this.crt_user_name = crt_user_name;
    }

    public String getCrt_user_id() {
        return crt_user_id;
    }

    public void setCrt_user_id(String crt_user_id) {
        this.crt_user_id = crt_user_id;
    }

    public Date getCrt_time() {
        return crt_time;
    }

    public void setCrt_time(Date crt_time) {
        this.crt_time = crt_time;
    }

    public String getUpd_user_name() {
        return upd_user_name;
    }

    public void setUpd_user_name(String upd_user_name) {
        this.upd_user_name = upd_user_name;
    }

    public String getUpd_user_id() {
        return upd_user_id;
    }

    public void setUpd_user_id(String upd_user_id) {
        this.upd_user_id = upd_user_id;
    }

    public Date getUpd_time() {
        return upd_time;
    }

    public void setUpd_time(Date upd_time) {
        this.upd_time = upd_time;
    }
}
