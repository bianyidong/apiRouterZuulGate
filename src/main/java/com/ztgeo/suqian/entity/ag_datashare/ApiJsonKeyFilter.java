package com.ztgeo.suqian.entity.ag_datashare;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Table(name = "api_json_key_filter")
@Entity
@Data
public class ApiJsonKeyFilter implements Serializable {
  private static final long serialVersionUID = 1L;

  /**
   * 表主键
   */
  @Id
  @Column(name = "id", insertable = false, nullable = false)
  private String id;

  /**
   * 接口id
   */
  @Column(name = "api_id")
  private String apiId;

  /**
   * 针对哪个用户进行过滤（已经在平台注册用户）
   */
  @Column(name = "from_user")
  private String fromUser;

  /**
   * json字串过滤的key集合，存储时使用英文逗号隔开
   */
  @Column(name = "field_list")
  private String fieldList;

  
}