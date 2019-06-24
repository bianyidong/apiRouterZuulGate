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
@Entity(name = "user_key_info")
public class UserKeyInfo implements Serializable {
    @Id
    @Column(name = "key_id")
    private String keyId;
    @Column(name = "user_real_id")
    private String userRealId;
    @Column(name = "username")
    private String username;
    @Column(name = "name")
    private String name;
    @Column(name = "user_identity_id")
    private String userIdentityId;
    @Column(name = "symmetric_pubkey")
    private String symmetricPubkey;
    @Column(name = "sign_secret_key")
    private String signSecretKey;
    @Column(name = "sign_pub_key")
    private String signPubKey;
    @Column(name = "sign_pt_secret_key")
    private String signPtSecretKey;
    @Column(name = "sign_pt_pub_key")
    private String signPtPubKey;
    @Column(name = "crt_time")
    private Date crtTime;
    @Column(name = "crt_user_id")
    private String crtUserId;
    @Column(name = "upd_time")
    private Date updTime;
    @Column(name = "upd_user_id")
    private String updUserId;
}
