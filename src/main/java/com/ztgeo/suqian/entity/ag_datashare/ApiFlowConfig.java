package com.ztgeo.suqian.entity.ag_datashare;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "api_flow_config")
public class ApiFlowConfig implements Serializable {

  /**
   * 主键
   */
  @Id
  @Column(name = "id", insertable = false, nullable = false)
  private String id;

  @Column(name = "api_id", nullable = false)
  private String apiId;

  /**
   * 限流对象，api,ip
   */
  @Column(name = "limit_object", nullable = false)
  private String limitObject;

  /**
   * 限流对象值，api值、ip值（字段）
   */
  @Column(name = "limit_value")
  private String limitValue;

  /**
   * 限流类型，时分秒等
   */
  @Column(name = "limit_type")
  private String limitType;

  /**
   * 限流次数
   */
  @Column(name = "limit_count")
  private Integer limitCount;

  /**
   * 限流次数
   */
  @Column(name = "limit_total_count")
  private Integer limitTotalCount;


  /**
   * 限流间隔
   */
  @Column(name = "jiange")
  private Integer jiange;

  
}