package com.c332030.ctool4j.mybatisplus.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.c332030.ctool4j.definition.entity.base.CId;
import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import com.c332030.ctool4j.doc.annotation.COperation;
import com.c332030.ctool4j.mybatis.model.impl.CPageReq;
import com.c332030.ctool4j.mybatisplus.service.ICService;
import com.c332030.ctool4j.spring.lifecycle.ICSpringInit;
import lombok.CustomLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.constraints.NotNull;

/**
 * <p>
 * Description: CMpController 基础控制器抽象类
 * </p>
 *
 * <p>提供分页查询、按 id 查询、新增、按 id 更新、按 id 删除等公共 CRUD 接口。</p>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li>提供分页查询、按 id 查询等公共接口。</li>
 *   <li>各接口方法标注 {@code @COperation} 提供接口摘要/说明（ctool4j-definition 的接口文档注解），供 doc-openapi2 生成接口文档。</li>
 *   <li>{@code @RequestBody} 参数不再标注参数注解：入参/出参文档由对应请求/响应 model 在父接口 getter 上的 {@code @CSchema}
 *   （如 ICPage、ICCode/ICMessage/ICData、ICId）统一提供，子类/实现无需重复标注。</li>
 *   <li>依赖以 {@code provided} 引入（{@code ctool4j-definition}），仅编译期生效、不透传；运行时由启用 doc-openapi2 的应用经其传递引入。</li>
 *   <li>本类为纯内部接口（供内部/管理端调用）。子类若需限制仅内网 IP/IP 段可访问，可在具体 Controller 类或其方法上标注 {@code @CInnerApi}
 *   （ctool4j-definition 的内部接口标记注解），由 ctool4j-web 的 {@code CInnerApiInterceptor} 读取并通过 {@code CInnerApiConfig}
 *   （{@code inner-api.allowed-ips}，支持单 IP 与 CIDR 网段）统一校验白名单；基类默认不标注，由使用方按需启用。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <p>实体简单名称初始化：{@link #onInit()} 中调用 {@code service.getEntitySimpleName()} 设置 {@link #entityName}。</p>
 *
 * <h2>适用范围</h2>
 * <p>控制器基类（抽象类，不可直接实例化）。</p>
 *
 * <h2>已知限制与取舍</h2>
 * <p>本类为抽象类，需子类提供具体 {@code ICService} 实现。</p>
 *
 * @since 2026/1/20
 * @version 1.0
 */
@CustomLog
public abstract class CMpController<S extends ICService<T>, T> implements ICSpringInit {

    /**
     * 业务服务
     */
    @Autowired
    protected S service;

    /**
     * 实体简单名称，初始化时设置
     */
    protected String entityName;

    /**
     * Spring 启动初始化回调：设置实体简单名称
     */
    @Override
    public void onInit() {
        entityName = service.getEntitySimpleName();
    }

    /**
     * 分页查询
     *
     * @param cPage 分页查询条件
     * @return 分页结果
     */
    @COperation("分页查询")
    @ResponseBody
    @PostMapping("/page")
    public CStrResult<IPage<T>> page(@Validated @NotNull @RequestBody CPageReq<T> cPage) {

        log.info("{} cPage: {}", entityName, cPage);
        return CStrResult.success(service.page(cPage));
    }

    /**
     * 按 id 查询
     *
     * @param cId id 请求
     * @return 查询结果
     */
    @COperation("按 id 查询")
    @ResponseBody
    @PostMapping("/get-by-id")
    public CStrResult<T> getById(@Validated @NotNull @RequestBody CId<?> cId) {
        log.info("{} getById cId: {}", entityName, cId);
        return CStrResult.success(service.getById(cId.getId()));
    }

    /**
     * 新增实体
     *
     * @param entity 实体
     * @return 新增结果
     */
    @COperation("新增实体")
    @ResponseBody
    @PostMapping("/add")
    public CStrResult<T> add(@Validated @NotNull @RequestBody T entity) {
        log.info("{} add entity: {}", entityName, entity);
        service.save(entity);
        return CStrResult.success(entity);
    }

    /**
     * 按 id 更新实体
     *
     * @param entity 实体
     * @return 更新结果
     */
    @COperation("按 id 更新")
    @ResponseBody
    @PostMapping("/update-by-id")
    public CStrResult<Boolean> updateById(@Validated @NotNull @RequestBody T entity) {
        log.info("{} updateById entity: {}", entityName, entity);
        return CStrResult.success(service.updateById(entity));
    }

    /**
     * 按 id 删除
     *
     * @param cId id 请求
     * @return 删除结果
     */
    @COperation("按 id 删除")
    @ResponseBody
    @PostMapping("/remove-by-id")
    public CStrResult<Boolean> removeById(@Validated @NotNull @RequestBody CId<?> cId) {
        log.info("{} removeById cId: {}", entityName, cId);
        return CStrResult.success(service.removeById(cId.getId()));
    }

}
