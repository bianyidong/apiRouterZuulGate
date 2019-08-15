package com.ztgeo.suqian.entity.ag_datashare;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@Entity
@Table(name = "dz_yixing")
public class DzYixing implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @Column(name = "id", insertable = false, nullable = false)
  private String id;

  @Column(name = "api_id", nullable = false)
  private String apiId;

  @Column(name = "from_user")
  private String fromUser;

  @Column(name = "dzurl")
  private String url;

  @Column(name = "content_type")
  private String contentType;

  @Column(name = "dzmethod")
  private String method;

  @Column(name = "soapbody_req")
  private String soapbodyReq;

  @Column(name = "soapbody_resp")
  private String soapbodyResp;

  @Column(name = "note")
  private String note;

  
}