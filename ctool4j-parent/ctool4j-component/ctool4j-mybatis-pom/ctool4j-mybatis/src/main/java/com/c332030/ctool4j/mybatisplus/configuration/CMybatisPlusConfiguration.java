package com.c332030.ctool4j.mybatisplus.configuration;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.c332030.ctool4j.core.util.CCollUtils;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * <p>
 * Description: CMybatisPlusConfiguration
 * </p>
 *
 * @since 2025/12/29
 */
@Configuration
public class CMybatisPlusConfiguration {

    @Bean
    @ConditionalOnMissingBean(MybatisPlusInterceptor.class)
    public MybatisPlusInterceptor cMybatisPlusInterceptor(
        @Autowired(required = false) DynamicTableNameInnerInterceptor dynamicTableNameInnerInterceptor,
        @Autowired(required = false) PaginationInnerInterceptor paginationInnerInterceptor,
        @Autowired(required = false) Collection<InnerInterceptor> innerInterceptors
    ) {

        val interceptors = new LinkedHashSet<>(innerInterceptors);
        CCollUtils.addIgnoreNull(interceptors, dynamicTableNameInnerInterceptor);
        CCollUtils.addIgnoreNull(interceptors, paginationInnerInterceptor);
        CCollUtils.addAllIgnoreNull(interceptors, innerInterceptors);

        val mybatisPlusInterceptor = new MybatisPlusInterceptor();
        mybatisPlusInterceptor.setInterceptors(new ArrayList<>(interceptors));
        return mybatisPlusInterceptor;
    }

    @Bean
    @ConditionalOnBean(MybatisPlusInterceptor.class)
    @ConditionalOnMissingBean(PaginationInnerInterceptor.class)
    public PaginationInnerInterceptor cPaginationInnerInterceptor() {
        return new PaginationInnerInterceptor();
    }

    @Bean
    @ConditionalOnBean(MybatisPlusInterceptor.class)
    @ConditionalOnMissingBean(BlockAttackInnerInterceptor.class)
    public BlockAttackInnerInterceptor cBlockAttackInnerInterceptor() {
        return new BlockAttackInnerInterceptor();
    }

}
