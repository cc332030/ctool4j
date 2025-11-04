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

    /**
     * 判断 key 是否无效，重写 String 类型的 key
     * @param key key
     * @return 有效性
     */
    @Override
    public boolean isInvalidKey(String key) {
        return StrUtil.isBlank(key);
    }

    /**
     * 判断 value 是否无效，重写 String 值的判断
     * @param value 值
     * @return 有效性
     */
    @Override
    public boolean isInvalidValue(String value) {
        return StrUtil.isBlank(value);
    }

}
