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
@Entity(name = "api_notionalshared_config")
public class ApiNotionalSharedConfig implements Serializable {
    @Id
    @Column(name = "userid")
    private String userid;
    @Column(name = "id")
    private String id;
    @Column(name = "token")
    private String token;
    @Column(name = "dept_name")
    private String deptName;
    @Column(name = "qxdm")
    private String qxdm;
}
