package com.ztgeo.suqian.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class NoticeTypeInfo implements Serializable {

    @Id
    private String ID;
    private String type_id;
    private String type_desc;
    private Date crt_time;
    private String crt_user_id;
    private Date upd_time;
    private String upd_user_id;

}
