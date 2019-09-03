package com.ztgeo.suqian.repository;
import com.ztgeo.suqian.entity.ag_datashare.NoticeBaseInfo;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface NoticeBaseInfoRepository extends CrudRepository<NoticeBaseInfo,String> {

    @Query(nativeQuery = true,value = "select nbi.* from notice_base_info nbi inner join notice_user_rel nur on nbi.notice_id = nur.notice_id inner join user_key_info uki on nur.user_real_id = uki.user_real_id where uki.user_real_id = ? and nur.type_id = ?")
    List<NoticeBaseInfo> querySendUrl(String userID,String noticeCode);
}
