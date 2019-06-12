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
@Entity
public class BlackListIp implements Serializable {
    @Id
    private String ip_id;
    private Long ip_content;
    private String ip_note;
    private String crt_user_id;
    private Date crt_time;
    private String upd_user_id;
    private Date upd_time;

    public String getIp_id() {
        return ip_id;
    }

    public void setIp_id(String ip_id) {
        this.ip_id = ip_id;
    }

    public Long getIp_content() {
        return ip_content;
    }

    public void setIp_content(Long ip_content) {
        this.ip_content = ip_content;
    }

    public String getIp_note() {
        return ip_note;
    }

    public void setIp_note(String ip_note) {
        this.ip_note = ip_note;
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
