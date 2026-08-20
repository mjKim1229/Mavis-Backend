package com.mavis.domain.global.config;

import net.ttddyy.dsproxy.listener.logging.SLF4JLogLevel;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class DataSourceProxyBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof DataSource dataSource) {
            return ProxyDataSourceBuilder
                    .create(dataSource)
                    .name("mavis")
                    .logQueryBySlf4j(SLF4JLogLevel.DEBUG, "SQL")
                    .multiline()
                    .build();
        }
        return bean;
    }
}
