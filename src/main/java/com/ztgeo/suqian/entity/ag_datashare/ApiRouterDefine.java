package com.ztgeo.suqian.entity.ag_datashare;

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
public class ApiRouterDefine implements Serializable {
    @Id
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

}
