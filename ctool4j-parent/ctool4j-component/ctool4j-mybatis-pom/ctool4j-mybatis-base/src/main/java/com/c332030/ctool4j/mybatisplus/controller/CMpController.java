package com.c332030.ctool4j.mybatisplus.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.c332030.ctool4j.definition.entity.base.CId;
import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import com.c332030.ctool4j.mybatis.model.impl.CPageReq;
import com.c332030.ctool4j.mybatisplus.service.ICBaseService;
import com.c332030.ctool4j.spring.lifecycle.ICSpringInit;
import lombok.CustomLog;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.constraints.NotNull;

/**
 * <p>
 * Description: CMpController
 * </p>
 *
 * @since 2026/1/20
 */
@CustomLog
public abstract class CMpController<S extends ICBaseService<T>, T> implements ICSpringInit {

    @Setter(onMethod_ = @Autowired)
    protected S service;

    protected String entityName;

    @Override
    public void onInit() {
        entityName = service.getEntitySimpleName();
    }

    @ResponseBody
    @PostMapping("/page")
    public CStrResult<IPage<T>> page(@Validated @NotNull @RequestBody CPageReq<T> cPage) {

        log.info("{} cPage: {}", entityName, cPage);
        return CStrResult.success(service.page(cPage));
    }

    @ResponseBody
    @PostMapping("/get-by-id")
    public CStrResult<T> getById(@Validated @NotNull @RequestBody CId<?> cId) {
        log.info("{} getById cId: {}", entityName, cId);
        return CStrResult.success(service.getById(cId.getId()));
    }

    @ResponseBody
    @PostMapping("/add")
    public CStrResult<T> add(@Validated @NotNull @RequestBody T entity) {
        log.info("{} add entity: {}", entityName, entity);
        service.save(entity);
        return CStrResult.success(entity);
    }

    @ResponseBody
    @PostMapping("/update-by-id")
    public CStrResult<Boolean> updateById(@Validated @NotNull @RequestBody T entity) {
        log.info("{} updateById entity: {}", entityName, entity);
        return CStrResult.success(service.updateById(entity));
    }

    @ResponseBody
    @PostMapping("/remove-by-id")
    public CStrResult<Boolean> removeById(@Validated @NotNull @RequestBody CId<?> cId) {
        log.info("{} removeById cId: {}", entityName, cId);
        return CStrResult.success(service.removeById(cId.getId()));
    }

}
