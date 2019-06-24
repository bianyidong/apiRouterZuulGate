package com.ztgeo.suqian.entity.ag_datashare;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "api_router_define")
public class ApiRouterDefine implements Serializable {
    @Id
    @Column(name = "id")
    private String id;
    @Column(name = "path")
    private String path;
    @Column(name = "service_id")
    private String serviceId;
    @Column(name = "url")
    private String url;
    @Column(name = "retryable")
    private Integer retryable;
    @Column(name = "enabled")
    private Integer enabled;
    @Column(name = "strip_prefix")
    private Integer stripPrefix;
    @Column(name = "crt_user_name")
    private String crtUserName;
    @Column(name = "crt_user_id")
    private String crtUserId;
    @Column(name = "crt_time")
    private Date crtTime;
    @Column(name = "upd_user_name")
    private String updUserName;
    @Column(name = "upd_user_id")
    private String updUserId;
    @Column(name = "upd_time")
    private Date updTime;
}
