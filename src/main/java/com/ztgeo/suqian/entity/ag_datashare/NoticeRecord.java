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
public class NoticeRecord {

    @Id
    private String record_id;
    private String sender_id;
    private String receiver_id;
    private String receiver_url;
    private String receiver_usename;
    private String receiver_name;
    private String typedesc;
    private Integer status;
    private Date send_time;
    private Integer count;
    private String request_data;

}
