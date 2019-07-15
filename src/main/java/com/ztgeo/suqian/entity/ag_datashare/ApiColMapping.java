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
@Entity(name = "api_col_mapping")
public class ApiColMapping {
    @Id
    @Column(name = "id")
    private String id;
    @Column(name = "type_id")
    private String typeId;
    @Column(name = "from_col")
    private String fromCol;
    @Column(name = "to_col")
    private String toCol;
    @Column(name = "http_type")
    private String httpType;

}
