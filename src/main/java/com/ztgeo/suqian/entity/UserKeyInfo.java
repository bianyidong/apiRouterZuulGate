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

}
