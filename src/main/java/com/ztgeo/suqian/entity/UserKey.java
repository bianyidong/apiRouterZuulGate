package com.ztgeo.suqian.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;

public class UserKey implements Serializable {
    private String user_real_id;
    private String symmetric_pubkey;
    private String sign_secret_key;
    private String sign_pub_key;
    private String sign_pt_secret_key;
    private String sign_pt_pub_key;

    public String getUser_real_id() {
        return user_real_id;
    }

    public void setUser_real_id(String user_real_id) {
        this.user_real_id = user_real_id;
    }

    public String getSymmetric_pubkey() {
        return symmetric_pubkey;
    }

    public void setSymmetric_pubkey(String symmetric_pubkey) {
        this.symmetric_pubkey = symmetric_pubkey;
    }

    public String getSign_secret_key() {
        return sign_secret_key;
    }

    public void setSign_secret_key(String sign_secret_key) {
        this.sign_secret_key = sign_secret_key;
    }

    public String getSign_pub_key() {
        return sign_pub_key;
    }

    public void setSign_pub_key(String sign_pub_key) {
        this.sign_pub_key = sign_pub_key;
    }

    public String getSign_pt_secret_key() {
        return sign_pt_secret_key;
    }

    public void setSign_pt_secret_key(String sign_pt_secret_key) {
        this.sign_pt_secret_key = sign_pt_secret_key;
    }

    public String getSign_pt_pub_key() {
        return sign_pt_pub_key;
    }

    public void setSign_pt_pub_key(String sign_pt_pub_key) {
        this.sign_pt_pub_key = sign_pt_pub_key;
    }

    @Override
    public String toString() {
        return "UserKey{" +
                "user_real_id='" + user_real_id + '\'' +
                ", symmetric_pubkey='" + symmetric_pubkey + '\'' +
                ", sign_secret_key='" + sign_secret_key + '\'' +
                ", sign_pub_key='" + sign_pub_key + '\'' +
                ", sign_pt_secret_key='" + sign_pt_secret_key + '\'' +
                ", sign_pt_pub_key='" + sign_pt_pub_key + '\'' +
                '}';
    }
}



