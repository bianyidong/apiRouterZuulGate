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
public class UserKeyInfo implements Serializable {
    @Id
    private String key_id;
    private String user_real_id;
    private String username;
    private String name;
    private String user_identity_id;
    private String symmetric_pubkey;
    private String sign_secret_key;
    private String sign_pub_key;
    private Date crt_time;
    private String crt_user_id;
    private Date upd_time;
    private String upd_user_id;

    public String getKey_id() {
        return key_id;
    }

    public void setKey_id(String key_id) {
        this.key_id = key_id;
    }

    public String getUser_real_id() {
        return user_real_id;
    }

    public void setUser_real_id(String user_real_id) {
        this.user_real_id = user_real_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUser_identity_id() {
        return user_identity_id;
    }

    public void setUser_identity_id(String user_identity_id) {
        this.user_identity_id = user_identity_id;
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
