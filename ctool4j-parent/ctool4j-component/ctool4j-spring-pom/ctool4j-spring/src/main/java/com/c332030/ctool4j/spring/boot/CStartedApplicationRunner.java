package com.c332030.ctool4j.spring.boot;

import com.c332030.ctool4j.core.log.CLogUtils;
import com.c332030.ctool4j.spring.util.CSpringUtils;
import lombok.val;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

/**
 * <p>
 * Description: CStartedApplicationRunner
 * </p>
 *
 * @since 2026/5/12
 * @see "doc/design/spring/CStartedApplicationRunner.adoc"
 */
public interface CStartedApplicationRunner extends ApplicationRunner {

    /**
     * 应用启动后输出启动成功日志
     * @param args 启动参数
     */
    @Override
    default void run(ApplicationArguments args) {

        val log = CLogUtils.getLog(CStartedApplicationRunner.class);
        log.info("(♥◠‿◠)ﾉﾞ  {} 启动成功  ლ(´ڡ`ლ)ﾞ", CSpringUtils.getApplicationName());

    }

}
