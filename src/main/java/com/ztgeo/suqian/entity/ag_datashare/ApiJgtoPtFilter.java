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
@Entity(name = "api_jgtopt_filter")
public class ApiJgtoPtFilter implements Serializable {
    @Id
    @Column(name = "id")
    private String Id;
    @Column(name = "from_user")
    private String fromUser;
    @Column(name = "uri")
    private String uri;
    @Column(name = "sym_pubkey")
    private String symPubkey;
    @Column(name = "secret_key")
    private String secretKey;
    @Column(name = "pub_key")
    private String pubKey;
    @Column(name = "pt_secret_key")
    private String ptSecretKey;
    @Column(name = "pt_pub_key")
    private String ptPubKey;
}
