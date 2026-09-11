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
 * <h2>设计要点</h2>
 * <ul>
 *   <li>ApplicationRunner，应用启动完成后执行 onInit</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>启动完成即执行</p>
 * <h2>适用范围</h2>
 * <p>启动后初始化</p>
 * <h2>不适用与边界场景</h2>
 * <p>实现 ApplicationRunner</p>
 * <h2>已知限制与取舍</h2>
 * <p>实现 ApplicationRunner</p>
 *
 * @since 2026/5/12
 * @version 1.0
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
