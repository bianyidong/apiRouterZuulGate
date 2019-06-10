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
public class ApiBaseInfo implements Serializable {
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

}
