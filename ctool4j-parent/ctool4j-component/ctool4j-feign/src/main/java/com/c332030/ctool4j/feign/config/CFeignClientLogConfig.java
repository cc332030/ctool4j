package com.c332030.ctool4j.feign.config;

import com.c332030.ctool4j.core.util.CSet;
import com.c332030.ctool4j.web.config.CRequestLogBaseConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

/**
 * <p>
 * Description: CFeignClientLogConfig
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CFeignClientLogConfig}（{@code @ConfigurationProperties("feign.client.log")}）为 Feign 日志配置， 公共属性（{@code enable}/{@code enableHeader}/{@code slowLogEnable}/{@code slowLogMillis}）继承自 {@code CRequestLogBaseConfig}，本类维护 Feign 特有属性：</p>
 * <ul>
 *   <li>{@code logAll}：全部日志开关；{@code apiWhiteList}/{@code apiBlackList}：接口白/黑名单。</li>
 *   <li>{@code hostWhiteList}/{@code hostBlackList}：host 白/黑名单；{@code pathWhiteList}/{@code pathBlackList}：path 白/黑名单。</li>
 * </ul>
 * <p>继承自基类的属性：</p>
 * <ul>
 *   <li>{@code enable}：日志开关（默认 false）；{@code enableHeader}：请求头日志开关（默认 false）。</li>
 *   <li>{@code slowLogEnable}：慢请求日志开关（默认 true，不受 enable 控制）；{@code slowLogMillis}：慢请求日志毫秒数（默认 3000）。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>前缀 {@code feign.client.log}，供配置文件绑定。</li>
 *   <li>集合默认 {@code CSet.of()}（空不可变集合）。</li>
 *   <li>公共属性（enable/enableHeader/slowLogEnable/slowLogMillis）上移至 {@code CRequestLogBaseConfig}，避免与 web 配置重复声明。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <ul>
 *   <li>未配置时用默认值（enable=false、slowLogEnable=true、空名单）。</li>
 * </ul>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>配置 Feign 日志开关与白/黑名单过滤。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>仅配置载体；实际日志行为由 {@code CFeignLogger}/{@code CFeignClient} 使用。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>集合默认不可变，动态修改需重新 set。</li>
 *   <li>原 {@code enableCost}（耗时日志开关）经确认在业务代码中从未被使用，重构时已删除。</li>
 * </ul>
 *
 * @since 2025/9/21
 * @version 1.0
 */
@Data
@ConfigurationProperties("feign.client.log")
public class CFeignClientLogConfig extends CRequestLogBaseConfig {

    /**
     * 全部日志开关
     */
    Boolean logAll = false;

    /**
     * 接口白名单
     */
    Set<String> apiWhiteList = CSet.of();

    /**
     * 接口黑名单
     */
    Set<String> apiBlackList = CSet.of();

    /**
     * host 白名单
     */
    Set<String> hostWhiteList = CSet.of();

    /**
     * host 黑名单
     */
    Set<String> hostBlackList = CSet.of();

    /**
     * path 白名单
     */
    Set<String> pathWhiteList = CSet.of();

    /**
     * path 黑名单
     */
    Set<String> pathBlackList = CSet.of();

}
