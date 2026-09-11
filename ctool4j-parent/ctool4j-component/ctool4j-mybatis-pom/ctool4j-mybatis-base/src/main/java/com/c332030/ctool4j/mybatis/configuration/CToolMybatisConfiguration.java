package com.c332030.ctool4j.mybatis.configuration;

import com.baomidou.mybatisplus.annotation.TableName;
import com.c332030.ctool4j.core.log.CLogUtils;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * Description: CToolMybatisConfiguration
 * </p>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li>@Configuration，静态块注册 @TableName 为 JSON 日志注解</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>无</p>
 * <h2>适用范围</h2>
 * <p>MyBatis 相关配置</p>
 * <h2>不适用与边界场景</h2>
 * <p>依赖 CLogUtils</p>
 * <h2>已知限制与取舍</h2>
 * <p>依赖 CLogUtils</p>
 *
 * @since 2025/9/14
 * @version 1.0
 */
@Configuration
public class CToolMybatisConfiguration {

    static {
        CLogUtils.addJsonLogAnnotations(TableName.class);
    }

}
