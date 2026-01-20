package com.c332030.ctool4j.mybatisplus.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.c332030.ctool4j.definition.entity.base.CId;
import com.c332030.ctool4j.definition.model.result.impl.CIntResult;
import com.c332030.ctool4j.mybatis.model.impl.CPage;
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
    public CIntResult<IPage<T>> page(@Validated @NotNull @RequestBody CPage cPage) {
        log.info("{} cPage: {}", entityName, cPage);
        return CIntResult.success(service.page(cPage.getPage()));
    }

    @ResponseBody
    @PostMapping("/get-by-id")
    public CIntResult<T> getById(@Validated @NotNull @RequestBody CId<?> cId) {
        log.info("{} get-by-id, cId: {}", entityName, cId);
        return CIntResult.success(service.getById(cId.getId()));
    }

    @ResponseBody
    @PostMapping("/add")
    public CIntResult<T> add(@Validated @NotNull @RequestBody T entity) {
        log.info("{} add entity: {}", entityName, entity);
        service.save(entity);
        return CIntResult.success(entity);
    }

    @ResponseBody
    @PostMapping("/update-by-id")
    public CIntResult<Boolean> updateById(@Validated @NotNull @RequestBody T entity) {
        log.info("{} update-by-id entity: {}", entityName, entity);
        return CIntResult.success(service.updateById(entity));
    }

    @ResponseBody
    @PostMapping("/remove-by-id")
    public CIntResult<Boolean> removeById(@Validated @NotNull @RequestBody CId<?> cId) {
        log.info("{} remove-by-id, cId: {}", entityName, cId);
        return CIntResult.success(service.removeById(cId.getId()));
    }

}
