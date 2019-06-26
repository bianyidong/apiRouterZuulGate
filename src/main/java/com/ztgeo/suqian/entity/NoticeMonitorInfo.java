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

public class NoticeMonitorInfo implements Serializable {

    private String id_;
    private String sender_user_id;
    private Date last_send_time;
    private String send_status;
    private Date crt_time;
    private Date upd_time;

    public String getId_() {
        return id_;
    }

    public void setId_(String id_) {
        this.id_ = id_;
    }

    public String getSender_user_id() {
        return sender_user_id;
    }

    public void setSender_user_id(String sender_user_id) {
        this.sender_user_id = sender_user_id;
    }

    public Date getLast_send_time() {
        return last_send_time;
    }

    public void setLast_send_time(Date last_send_time) {
        this.last_send_time = last_send_time;
    }

    public String getSend_status() {
        return send_status;
    }

    public void setSend_status(String send_status) {
        this.send_status = send_status;
    }

    public Date getCrt_time() {
        return crt_time;
    }

    public void setCrt_time(Date crt_time) {
        this.crt_time = crt_time;
    }

    public Date getUpd_time() {
        return upd_time;
    }

    public void setUpd_time(Date upd_time) {
        this.upd_time = upd_time;
    }
}
