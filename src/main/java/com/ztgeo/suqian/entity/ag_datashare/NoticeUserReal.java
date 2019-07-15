package com.ztgeo.suqian.entity.ag_datashare;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "notice_user_rel")
public class NoticeUserReal implements Serializable {

    @Id
    @Column(name = "rel_id")
    private String relId;
    @Column(name = "type_id")
    private String typeId;
    @Column(name = "user_real_id")
    private String userRealId;
    @Column(name = "notice_id")
    private String noticeId;
}
