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

public class NoticeBaseInfo implements Serializable {

    private String notice_id;
    private String user_real_id;
    private String name;
    private String username;
    private String notice_path;
    private String method;
    private String type_id;
    private String notice_note;
    private Date crt_time;
    private String crt_user_id;
    private Date upd_time;
    private String upd_user_id;

    public String getNotice_id() {
        return notice_id;
    }

    public void setNotice_id(String notice_id) {
        this.notice_id = notice_id;
    }

    public String getUser_real_id() {
        return user_real_id;
    }

    public void setUser_real_id(String user_real_id) {
        this.user_real_id = user_real_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNotice_path() {
        return notice_path;
    }

    public void setNotice_path(String notice_path) {
        this.notice_path = notice_path;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getType_id() {
        return type_id;
    }

    public void setType_id(String type_id) {
        this.type_id = type_id;
    }

    public String getNotice_note() {
        return notice_note;
    }

    public void setNotice_note(String notice_note) {
        this.notice_note = notice_note;
    }

    public Date getCrt_time() {
        return crt_time;
    }

    public void setCrt_time(Date crt_time) {
        this.crt_time = crt_time;
    }

    public String getCrt_user_id() {
        return crt_user_id;
    }

    public void setCrt_user_id(String crt_user_id) {
        this.crt_user_id = crt_user_id;
    }

    public Date getUpd_time() {
        return upd_time;
    }

    public void setUpd_time(Date upd_time) {
        this.upd_time = upd_time;
    }

    public String getUpd_user_id() {
        return upd_user_id;
    }

    public void setUpd_user_id(String upd_user_id) {
        this.upd_user_id = upd_user_id;
    }
}
