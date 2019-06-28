package com.ztgeo.suqian.repository;

import com.ztgeo.suqian.entity.ag_datashare.UserKeyInfo;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserKeyInfoRepository extends CrudRepository<UserKeyInfo,String> {

    int countUserKeyInfosByUserRealIdEquals(String userid);

    List<UserKeyInfo> findAll();
    UserKeyInfo findByUserRealIdEquals(String userid);

}
