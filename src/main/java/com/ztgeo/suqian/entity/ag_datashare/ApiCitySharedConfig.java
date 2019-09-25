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
@Entity(name = "api_cityshared_config")
public class ApiCitySharedConfig implements Serializable {
    @Id
    @Column(name = "api_id")
    private String apiId;
    @Column(name = "service_id")
    private String serviceId;
    @Column(name = "ak")
    private String ak;
    @Column(name = "sk")
    private String sk;
    @Column(name = "app_id")
    private String appId;
}
