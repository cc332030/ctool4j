package com.c332030.ctool4j.log.configuration;

import com.c332030.ctool4j.spring.lifecycle.ICSpringInit;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * Description: CLogInit
 * </p>
 *
 * @see "doc/design/log/CLogInit.adoc"
 * @since 2025/9/29
 */
@Configuration
@AllArgsConstructor
public class CLogInit implements ICSpringInit {

    /**
     * Spring 启动初始化回调（当前无处理逻辑）
     */
    @Override
    public void onInit() {

    }

}
