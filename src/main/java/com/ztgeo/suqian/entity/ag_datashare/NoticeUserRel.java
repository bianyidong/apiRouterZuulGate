package com.ztgeo.suqian.entity.ag_datashare;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class NoticeUserRel implements Serializable {

    @Id
    private String rel_id;
    private String type_id;
    private String user_real_id;
    private String notice_id;

}
