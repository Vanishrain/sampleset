package cn.iecas.sampleset.common.annotation;

import cn.iecas.sampleset.common.config.AuthenticationConfiguration;
import cn.iecas.sampleset.common.filter.AuthenticationFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({AuthenticationFilter.class,
        AuthenticationConfiguration.class,})
public @interface EnableSecurity {
}
