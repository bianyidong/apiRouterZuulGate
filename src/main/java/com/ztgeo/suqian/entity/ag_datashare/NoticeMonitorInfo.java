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
public class NoticeMonitorInfo {

    @Id
    private String id_;
    private String sender_user_id;
    private Date last_send_time;
    private String send_status;
    private Date crt_time;
    private Date upd_time;

}
