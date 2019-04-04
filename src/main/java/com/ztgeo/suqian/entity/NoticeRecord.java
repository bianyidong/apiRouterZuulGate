package com.ztgeo.suqian.entity;

import java.io.Serializable;
import java.util.Date;


/**
 * 通知记录
 * 
 * @author zoupeidong
 * @email 806316372@qq.com
 * @version 2018-09-17 11:52:26
 */
public class NoticeRecord implements Serializable {
	private static final long serialVersionUID = 1L;
	
    private String recordId;
	
	    //发送方公开ID
    private String senderId;
	
	    //接收方真实ID
    private String receiverId;
	
	    //接收方URL
    private String receiverUrl;
	
	    //是否发送成功(0-成功，1-失败)
    private int status;
	
	    //发送时间
    private Date sendTime;
	

	/**
	 * 设置：
	 */
	public void setRecordId(String recordId) {
		this.recordId = recordId;
	}
	/**
	 * 获取：
	 */
	public String getRecordId() {
		return recordId;
	}
	/**
	 * 设置：发送方公开ID
	 */
	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}
	/**
	 * 获取：发送方公开ID
	 */
	public String getSenderId() {
		return senderId;
	}
	/**
	 * 设置：接收方真实ID
	 */
	public void setReceiverId(String receiverId) {
		this.receiverId = receiverId;
	}
	/**
	 * 获取：接收方真实ID
	 */
	public String getReceiverId() {
		return receiverId;
	}
	/**
	 * 设置：接收方URL
	 */
	public void setReceiverUrl(String receiverUrl) {
		this.receiverUrl = receiverUrl;
	}
	/**
	 * 获取：接收方URL
	 */
	public String getReceiverUrl() {
		return receiverUrl;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * 设置：发送时间
	 */
	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}
	/**
	 * 获取：发送时间
	 */
	public Date getSendTime() {
		return sendTime;
	}
}
