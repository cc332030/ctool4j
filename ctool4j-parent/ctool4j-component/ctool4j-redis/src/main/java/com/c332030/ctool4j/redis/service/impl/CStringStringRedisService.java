package com.c332030.ctool4j.redis.service.impl;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.redis.service.CAbstractRedisService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Description: CStringStringRedisService
 * </p>
 *
 * @since 2025/11/4
 */
@Service
@AllArgsConstructor
public class CStringStringRedisService extends CAbstractRedisService<String, String> {

    @Override
    public boolean isInvalidKey(String key) {
        return StrUtil.isBlank(key);
    }

    @Override
    public boolean isInvalidValue(String value) {
        return StrUtil.isBlank(value);
    }

}
