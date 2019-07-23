package com.ztgeo.suqian.repository;

import com.ztgeo.suqian.entity.ag_datashare.NoticeRecord;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;
import java.util.List;


public interface NoticeRecordRepository extends CrudRepository<NoticeRecord, String> {
    List<NoticeRecord> findNoticeRecordsByStatusAndCountLessThan(int status, int count);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE notice_record SET status=0 WHERE record_id=?1",nativeQuery = true)
    int updateNoticeRecordStatusSuccess(String recordId);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE notice_record SET count=?1 WHERE record_id=?2",nativeQuery = true)
    int updateNoticeRecordCount(int count,String recordId);
}
