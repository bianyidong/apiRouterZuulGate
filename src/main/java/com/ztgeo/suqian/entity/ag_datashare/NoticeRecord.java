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
@Entity(name = "notice_record")
public class NoticeRecord implements Serializable {
    @Id
    @Column(name = "record_id")
    private String recordId;
    @Column(name = "sender_id")
    private String senderId;
    @Column(name = "receiver_id")
    private String receiverId;
    @Column(name = "receiver_url")
    private String receiverUrl;
    @Column(name = "receiver_usename")
    private String receiverUsename;
    @Column(name = "receiver_name")
    private String receiverName;
    @Column(name = "notice_Code")
    private String noticeCode;
    @Column(name = "typedesc")
    private String typedesc;
    @Column(name = "status")
    private Integer status;
    @Column(name="send_time")
    private String sendtime;
    @Column(name = "count")
    private Integer count;
    @Column(name = "request_data")
    private String requestData;

}
