package com.iboot.pf.framework.idempotent.config;

import com.iboot.pf.framework.idempotent.core.aop.IdempotentAspect;
import com.iboot.pf.framework.idempotent.core.keyresolver.impl.DefaultIdempotentKeyResolver;
import com.iboot.pf.framework.idempotent.core.keyresolver.impl.ExpressionIdempotentKeyResolver;
import com.iboot.pf.framework.idempotent.core.keyresolver.IdempotentKeyResolver;
import com.iboot.pf.framework.idempotent.core.keyresolver.impl.UserIdempotentKeyResolver;
import com.iboot.pf.framework.idempotent.core.redis.IdempotentRedisDAO;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import com.iboot.pf.framework.redis.config.IbootRedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

@AutoConfiguration(after = IbootRedisAutoConfiguration.class)
public class IbootIdempotentConfiguration {

    @Bean
    public IdempotentAspect idempotentAspect(List<IdempotentKeyResolver> keyResolvers, IdempotentRedisDAO idempotentRedisDAO) {
        return new IdempotentAspect(keyResolvers, idempotentRedisDAO);
    }

    @Bean
    public IdempotentRedisDAO idempotentRedisDAO(StringRedisTemplate stringRedisTemplate) {
        return new IdempotentRedisDAO(stringRedisTemplate);
    }

    // ========== 各种 IdempotentKeyResolver Bean ==========

    @Bean
    public DefaultIdempotentKeyResolver defaultIdempotentKeyResolver() {
        return new DefaultIdempotentKeyResolver();
    }

    @Bean
    public UserIdempotentKeyResolver userIdempotentKeyResolver() {
        return new UserIdempotentKeyResolver();
    }

    @Bean
    public ExpressionIdempotentKeyResolver expressionIdempotentKeyResolver() {
        return new ExpressionIdempotentKeyResolver();
    }

}
