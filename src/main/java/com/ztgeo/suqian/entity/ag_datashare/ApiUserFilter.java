package com.ztgeo.suqian.entity.ag_datashare;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "api_user_filter")
public class ApiUserFilter implements Serializable {
    @Id
    @Column(name = "id")
    private String id;
    @Column(name = "api_id")
    private String apiId;
    @Column(name = "filter_name")
    private String filterName;
    @Column(name = "filter_bc")
    private String filterBc;
    @Column(name = "filter_order")
    private Integer filterOrder;
}
