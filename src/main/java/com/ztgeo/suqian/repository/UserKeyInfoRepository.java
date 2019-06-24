package com.ztgeo.suqian.repository;

import com.ztgeo.suqian.entity.ag_datashare.UserKeyInfo;
import org.springframework.data.repository.CrudRepository;

public interface UserKeyInfoRepository extends CrudRepository<UserKeyInfo,String> {

    int countUserKeyInfosByUserRealIdEquals(String userid);
}
