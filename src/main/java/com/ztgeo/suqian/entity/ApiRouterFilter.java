package com.ztgeo.suqian.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiRouterFilter implements Serializable {
    private String id;
    private String filter_name;
    private String filter_bc;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFilter_name() {
        return filter_name;
    }

    public void setFilter_name(String filter_name) {
        this.filter_name = filter_name;
    }

    public String getFilter_bc() {
        return filter_bc;
    }

    public void setFilter_bc(String filter_bc) {
        this.filter_bc = filter_bc;
    }
}
