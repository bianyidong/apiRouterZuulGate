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
public class NoticeBaseInfo {

    @Id
    private String notice_id;
    private String user_real_id;
    private String name;
    private String username;
    private String notice_path;
    private String method;
    private String type_id;
    private String notice_note;
    private Date crt_time;
    private String crt_user_id;
    private Date upd_time;
    private String upd_user_id;

}
