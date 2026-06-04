package com.c332030.ctool4j.job.xxljob.init;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.job.xxljob.config.CXxlJobExecutorConfig;
import com.c332030.ctool4j.spring.lifecycle.ICSpringInit;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import lombok.val;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * <p>
 * Description: CXxlJobInit
 * </p>
 *
 * @since 2026/6/4
 */
@CustomLog
@Component
@AllArgsConstructor
public class CXxlJobInit implements ICSpringInit {

    CXxlJobExecutorConfig executorConfig;

    @Override
    public void onInit() {

        try {

            val logPath = executorConfig.getLogpath();
            if(StrUtil.isBlank(logPath)) {
                return;
            }

            val file = new File(logPath);
            if(file.exists()) {
                return;
            }

            log.info("创建日志文件夹，absolutePath: {}，结果：{}", file.getAbsolutePath(), file.mkdirs());

        } catch (Exception e) {
            log.error("CXxlJobInit error", e);
        }

    }

}
