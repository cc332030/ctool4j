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
 */
public interface CStartedApplicationRunner extends ApplicationRunner {

    @Override
    default void run(ApplicationArguments args) {

        val log = CLogUtils.getLog(CStartedApplicationRunner.class);
        log.info("(♥◠‿◠)ﾉﾞ  {} 启动成功  ლ(´ڡ`ლ)ﾞ", CSpringUtils.getApplicationName());

    }

}
