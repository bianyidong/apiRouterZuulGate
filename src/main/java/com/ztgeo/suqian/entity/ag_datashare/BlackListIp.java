package com.ztgeo.suqian.entity.ag_datashare;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class BlackListIp {
    @Id
    private String ip_id;
    private Long ip_content;
    private String ip_note;
    private String crt_user_id;
    private Date crt_time;
    private String upd_user_id;
    private Date upd_time;
}
