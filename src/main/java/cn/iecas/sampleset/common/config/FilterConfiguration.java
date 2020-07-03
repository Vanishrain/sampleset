package cn.iecas.sampleset.common.config;

import cn.iecas.sampleset.common.filter.AuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class FilterConfiguration {

    @Autowired
    AuthenticationFilter authenticationFilter;

    @Bean
    public FilterRegistrationBean buildAuthenticationFilter(){
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setOrder(3);
        filterRegistrationBean.setFilter(authenticationFilter);
        filterRegistrationBean.setName("AuthenticationFilter");
        filterRegistrationBean.addUrlPatterns("/*");
        return filterRegistrationBean;
    }

    @Bean
    public FilterRegistrationBean buildCorsFilter(){
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.addAllowedOrigin("*");
        source.registerCorsConfiguration("/**",corsConfiguration);
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(new CorsFilter(source));
        filterRegistrationBean.setOrder(1);
        return filterRegistrationBean;
    }

//    @Bean
//    public FilterRegistrationBean buildCorsFilter(){
//        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
//        filterRegistrationBean.setOrder(1);
//        filterRegistrationBean.setFilter(new CorsFilter());
//        filterRegistrationBean.setName("CorsFilter");
//        filterRegistrationBean.addUrlPatterns("/*");
//        return filterRegistrationBean;
//    }
}
