package com.c332030.ctool4j.feign.targets;

import feign.Request;
import feign.RequestTemplate;
import feign.Target;

/**
 * <p>
 * Description: ICTarget
 * </p>
 *
 * @since 2025/12/25
 */
public interface ICTarget<T> extends Target<T> {

    @Override
    Class<T> type();

    @Override
    String name();

    @Override
    default String url() {
        return null;
    }

    @Override
    default Request apply(RequestTemplate input) {
        return input.request();
    }

}
