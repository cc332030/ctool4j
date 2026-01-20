package com.c332030.ctool4j.mybatisplus.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.c332030.ctool4j.core.log.CLog;
import com.c332030.ctool4j.definition.model.result.impl.CIntResult;
import com.c332030.ctool4j.mybatis.model.impl.CPage;
import com.c332030.ctool4j.mybatisplus.mapper.CBaseMapper;
import com.c332030.ctool4j.mybatisplus.service.impl.CBaseServiceImpl;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.Serializable;

/**
 * <p>
 * Description: CMpController
 * </p>
 *
 * @since 2026/1/20
 */
public abstract class CMpController<S extends CBaseServiceImpl<? extends CBaseMapper<T>, T>, T> {

    @Setter(onMethod_ = @Autowired)
    protected S service;

    abstract CLog getLog();

    @PostMapping("/page")
    public CIntResult<IPage<T>> page(CPage cPage) {
        getLog().info("page: {}", cPage);
        return CIntResult.success(service.page(cPage.getPage()));
    }

    @PostMapping("/get-by-id")
    public CIntResult<T> getById(Serializable id) {
        getLog().info("get-by-id: {}", id);
        return CIntResult.success(service.getById(id));
    }

    @PostMapping("/add")
    public CIntResult<Void> page(T entity) {
        getLog().info("add entity: {}", entity);
        service.save(entity);
        return CIntResult.success();
    }

    @PostMapping("/update-by-id")
    public CIntResult<Boolean> updateById(T entity) {
        getLog().info("update-by-id entity: {}", entity);
        return CIntResult.success(service.updateById(entity));
    }

    @PostMapping("/remove-by-id")
    public CIntResult<Boolean> removeById(Serializable id) {
        getLog().info("remove-by-id: {}", id);
        return CIntResult.success(service.removeById(id));
    }

}
