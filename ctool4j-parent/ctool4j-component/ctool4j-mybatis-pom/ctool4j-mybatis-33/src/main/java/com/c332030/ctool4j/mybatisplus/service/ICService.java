package com.c332030.ctool4j.mybatisplus.service;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.c332030.ctool4j.core.classes.CBeanUtils;
import com.c332030.ctool4j.core.classes.CValidateUtils;
import com.c332030.ctool4j.mybatis.model.impl.CPageReq;
import lombok.val;

import java.util.Collection;

/**
 * <p>
 * Description: ICBaseService
 * </p>
 *
 * @since 2025/12/4
 */
public interface ICService<ENTITY> extends ICBaseService<ENTITY> {

    @Override
    default IPage<ENTITY> page(CPageReq<ENTITY> pageReq) {

        val reqMap = CBeanUtils.toMapUnderlineName(pageReq.getReq());
        if(CValidateUtils.isEmpty(reqMap)) {
            return page(pageReq.getPage());
        }

        val queryWrapper = new QueryWrapper<ENTITY>()
            .allEq(reqMap);

        return page(pageReq.getPage(), queryWrapper);
    }

    default Integer countByValue(ENTITY entity, SFunction<ENTITY, ?> column){
        if(null == entity) {
            return 0;
        }
        return countByValue(column, convertValue(entity, column));
    }
    default Integer countByValue(SFunction<ENTITY, ?> column, Object value){
        if(null == value) {
            return 0;
        }
        return lambdaQuery()
                .eq(column, value)
                .count();
    }

    default Integer countByValues(Collection<ENTITY> collection, SFunction<ENTITY, ?> column){

        if(CollUtil.isEmpty(collection)) {
            return 0;
        }

        val values = convertValues(collection, column);
        return countByValues(column, values);
    }

    default Integer countByValues(SFunction<ENTITY, ?> column, Collection<?> values){

        if(CollUtil.isEmpty(values)) {
            return 0;
        }
        return lambdaQuery()
                .in(column, values)
                .count();
    }

}
