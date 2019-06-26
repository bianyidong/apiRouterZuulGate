package com.ztgeo.suqian.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class NoticeUserRel implements Serializable {

    private String rel_id;
    private String type_id;
    private String user_real_id;
    private String notice_id;

    public String getRel_id() {
        return rel_id;
    }

    public void setRel_id(String rel_id) {
        this.rel_id = rel_id;
    }

    public String getType_id() {
        return type_id;
    }

    public void setType_id(String type_id) {
        this.type_id = type_id;
    }

    public String getUser_real_id() {
        return user_real_id;
    }

    public void setUser_real_id(String user_real_id) {
        this.user_real_id = user_real_id;
    }

    public String getNotice_id() {
        return notice_id;
    }

    public void setNotice_id(String notice_id) {
        this.notice_id = notice_id;
    }
}
