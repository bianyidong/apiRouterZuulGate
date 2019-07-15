package com.ztgeo.suqian.repository;

import com.ztgeo.suqian.entity.ag_datashare.ApiChangeType;
import org.springframework.data.repository.CrudRepository;

public interface ApiChangeTypeRepository extends CrudRepository<ApiChangeType, String> {

    /**
     *  按受理编号查询不动产办理进度接口
     */
    // 通管受理编号查询不动产抵押信息
//    @Query(value =  "SELECT DY.*\n" +
//                    "  FROM DJ_TSGL TSGL LEFT JOIN DJ_DY DY ON TSGL.SLBH = DY.SLBH\n" +
//                    " WHERE NVL(TSGL.LIFECYCLE, 0) = 0\n" +
//                    "   AND TSGL.DJZL = '抵押'\n" +
//                    "   AND TSGL.TSTYBM = ?",nativeQuery = true)
//    @Transactional
//    DJ_DY findDJ_DYInfoByslbh(String slbh);

    ApiChangeType findApiChangeTypesByApiIdEquals(String api_id);
}
