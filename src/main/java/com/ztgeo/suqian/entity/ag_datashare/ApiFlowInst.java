package com.ztgeo.suqian.entity.ag_datashare;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Table(name = "api_flow_inst")
@Entity
@Data
public class ApiFlowInst implements Serializable {
  private static final long serialVersionUID = 1L;

  /**
   * 主键
   */
  @Id
  @Column(name = "id", insertable = false, nullable = false)
  private String id;

  /**
   * 限流类型，api或ip
   */
  @Column(name = "type")
  private String type;

  @Column(name = "api_id")
  private String apiId;

  @Column(name = "ip")
  private String ip;

  /**
   * 访问时间，当前毫秒数
   */
  @Column(name = "start_time")
  private Long startTime;
  /**
   * 访问时间，当前毫秒数
   */
  @Column(name = "end_time")
  private Long endTime;

  /**
   * 当前访问次数
   */
  @Column(name = "current_count")
  private Integer currentCount;

  
}