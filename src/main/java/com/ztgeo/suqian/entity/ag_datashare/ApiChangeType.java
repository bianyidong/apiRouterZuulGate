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
@Entity(name = "api_change_type")
public class ApiChangeType {
    @Id
    @Column(name = "id")
    private String id;
    @Column(name = "api_id")
    private String apiId;
    @Column(name = "from_type")
    private String fromType;
    @Column(name = "to_type")
    private String to_type;
    @Column(name = "from_req_sample")
    private String fromReqSample;
    @Column(name = "from_resp_sample")
    private String fromRespSample;
    @Column(name = "to_req_sample")
    private String toReqSample;
    @Column(name = "to_resp_sample")
    private String toRespSample;
}
