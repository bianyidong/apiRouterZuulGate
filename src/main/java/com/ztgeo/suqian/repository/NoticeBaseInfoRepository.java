package com.ztgeo.suqian.repository;
import com.ztgeo.suqian.entity.ag_datashare.NoticeBaseInfo;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface NoticeBaseInfoRepository extends CrudRepository<NoticeBaseInfo,String> {

    @Query(value = "select notice_path noticePath,nbi.user_real_id userRealId,nbi.username username,nbi.name name,uki.user_real_id from notice_base_info nbi inner join notice_user_rel nur on nbi.notice_id = nur.notice_id inner join user_key_info uki on nur.user_real_id = uki.user_real_id where uki.user_identity_id = ? and nur.type_id = ?",nativeQuery = true)
    List<NoticeBaseInfo> querySendUrl(String userID,String noticeCode);
}
