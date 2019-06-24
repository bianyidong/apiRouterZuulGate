package com.ztgeo.suqian.entity.ag_datashare;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "api_time_filter")
public class ApiTimeFilter {

    @Id
    @Column(name = "id")
    private String id;
    @Column(name = "api_id")
    private String apiId;
    @Column(name = "stime")
    private String stime;
    @Column(name = "etime")
    private String etime;
}
