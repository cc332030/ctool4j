package com.c332030.mybatis.config;

import com.baomidou.mybatisplus.annotation.TableName;
import com.c332030.core.log.CLogUtils;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * Description: CToolMybatisConfiguration
 * </p>
 *
 * @since 2025/9/14
 */
@Configuration
public class CToolMybatisConfiguration {

    static {
        CLogUtils.addJsonLogAnnotations(TableName.class);
    }

}
