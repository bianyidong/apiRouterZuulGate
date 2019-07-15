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
@Entity(name = "notice_base_info")
public class NoticeBaseInfo implements Serializable {
    @Id
    @Column(name = "notice_id")
    private String noticeId;
    @Column(name = "user_real_id")
    private String userRealId;
    @Column(name = "name")
    private String name;
    @Column(name = "username")
    private String username;
    @Column(name = "notice_path")
    private String noticePath;
    @Column(name = "method")
    private String method;
    @Column(name = "type_id")
    private String typeId;
    @Column(name="notice_note")
    private String noticeNote;
    @Column(name = "crt_time")
    private Date crtTime;
    @Column(name = "crt_user_id")
    private String crtUserId;
    @Column(name = "upd_time")
    private Date updTime;
    @Column(name = "upd_user_id")
    private String updUserId;
}
