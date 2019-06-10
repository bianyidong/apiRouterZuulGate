package com.ztgeo.suqian.entity.ag_datashare;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ApiBaseInfo {
    @Id
    private String api_id;
    private String api_pubkey;
    private String api_name;
    private String base_url;
    private String path;
    private String method;
    private Integer enabled_status;
    private String responsible_person_name;
    private String responsible_person_tel;
    private String api_owner_id;
    private String api_owner_name;
    private String crt_user_id;
    private Date crt_time;
    private String upd_user_id;
    private Date upd_time;

    public String getApi_id() {
        return api_id;
    }

    public void setApi_id(String api_id) {
        this.api_id = api_id;
    }

    public String getApi_pubkey() {
        return api_pubkey;
    }

    public void setApi_pubkey(String api_pubkey) {
        this.api_pubkey = api_pubkey;
    }

    public String getApi_name() {
        return api_name;
    }

    public void setApi_name(String api_name) {
        this.api_name = api_name;
    }

    public String getBase_url() {
        return base_url;
    }

    public void setBase_url(String base_url) {
        this.base_url = base_url;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Integer getEnabled_status() {
        return enabled_status;
    }

    public void setEnabled_status(Integer enabled_status) {
        this.enabled_status = enabled_status;
    }

    public String getResponsible_person_name() {
        return responsible_person_name;
    }

    public void setResponsible_person_name(String responsible_person_name) {
        this.responsible_person_name = responsible_person_name;
    }

    public String getResponsible_person_tel() {
        return responsible_person_tel;
    }

    public void setResponsible_person_tel(String responsible_person_tel) {
        this.responsible_person_tel = responsible_person_tel;
    }

    public String getApi_owner_id() {
        return api_owner_id;
    }

    public void setApi_owner_id(String api_owner_id) {
        this.api_owner_id = api_owner_id;
    }

    public String getApi_owner_name() {
        return api_owner_name;
    }

    public void setApi_owner_name(String api_owner_name) {
        this.api_owner_name = api_owner_name;
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
