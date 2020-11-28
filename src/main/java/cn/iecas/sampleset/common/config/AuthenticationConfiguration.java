package cn.iecas.sampleset.common.config;

import cn.iecas.sampleset.common.filter.AuthenticationFilter;
import cn.iecas.sampleset.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

public class AuthenticationConfiguration {
    @Autowired
    UserInfoService userInfoService;

    @Bean
    public FilterRegistrationBean buildAuthenticationFilter(){
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        AuthenticationFilter authenticationFilter = new AuthenticationFilter();
        authenticationFilter.setUserInfoService(userInfoService);
        filterRegistrationBean.setOrder(3);
        filterRegistrationBean.setFilter(authenticationFilter);
        filterRegistrationBean.setName("AuthenticationFilter");
        filterRegistrationBean.addUrlPatterns("/*");
        return filterRegistrationBean;
    }
}
