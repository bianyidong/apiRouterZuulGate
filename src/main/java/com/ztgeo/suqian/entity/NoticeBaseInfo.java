package com.ztgeo.suqian.entity;

import java.io.Serializable;
import java.util.Date;


/**
 * 通知基础信息配置表
 * 
 * @author zoupeidong
 * @email 806316372@qq.com
 * @version 2018-09-14 11:58:02
 */
public class NoticeBaseInfo implements Serializable {
	private static final long serialVersionUID = 1L;
    private String noticeId;
    private String userRealId;

	// 用户登录名
	private String username;

	    //通知转发路径
    private String noticePath;
	
	    //http请求方法
    private String method;
	
	    //通知说明
    private String noticeNote;
	
	    //
    private Date crtTime;
	
	    //
    private String crtUserId;
	
	    //
    private Date updTime;
	
	    //
    private String updUserId;
	

	/**
	 * 设置：
	 */
	public void setNoticeId(String noticeId) {
		this.noticeId = noticeId;
	}
	/**
	 * 获取：
	 */
	public String getNoticeId() {
		return noticeId;
	}
	/**
	 * 设置：用户真实ID
	 */
	public void setUserRealId(String userRealId) {
		this.userRealId = userRealId;
	}
	/**
	 * 获取：用户真实ID
	 */
	public String getUserRealId() {
		return userRealId;
	}
	/**
	 * 设置：通知转发路径
	 */
	public void setNoticePath(String noticePath) {
		this.noticePath = noticePath;
	}
	/**
	 * 获取：通知转发路径
	 */
	public String getNoticePath() {
		return noticePath;
	}
	/**
	 * 设置：http请求方法
	 */
	public void setMethod(String method) {
		this.method = method;
	}
	/**
	 * 获取：http请求方法
	 */
	public String getMethod() {
		return method;
	}
	/**
	 * 设置：通知说明
	 */
	public void setNoticeNote(String noticeNote) {
		this.noticeNote = noticeNote;
	}
	/**
	 * 获取：通知说明
	 */
	public String getNoticeNote() {
		return noticeNote;
	}
	/**
	 * 设置：
	 */
	public void setCrtTime(Date crtTime) {
		this.crtTime = crtTime;
	}
	/**
	 * 获取：
	 */
	public Date getCrtTime() {
		return crtTime;
	}
	/**
	 * 设置：
	 */
	public void setCrtUserId(String crtUserId) {
		this.crtUserId = crtUserId;
	}
	/**
	 * 获取：
	 */
	public String getCrtUserId() {
		return crtUserId;
	}
	/**
	 * 设置：
	 */
	public void setUpdTime(Date updTime) {
		this.updTime = updTime;
	}
	/**
	 * 获取：
	 */
	public Date getUpdTime() {
		return updTime;
	}
	/**
	 * 设置：
	 */
	public void setUpdUserId(String updUserId) {
		this.updUserId = updUserId;
	}
	/**
	 * 获取：
	 */
	public String getUpdUserId() {
		return updUserId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

}
