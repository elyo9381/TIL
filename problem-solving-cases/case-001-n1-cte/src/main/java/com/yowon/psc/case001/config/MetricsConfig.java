package com.yowon.psc.case001.config;

import com.yowon.psc.common.metrics.SqlMetricsRunner;
import com.yowon.psc.common.metrics.SqlQueryExecutionListener;
import javax.sql.DataSource;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.config.BeanPostProcessor;

@Configuration
public class MetricsConfig {

    @Bean
    public SqlQueryExecutionListener sqlQueryExecutionListener() {
        return new SqlQueryExecutionListener();
    }

    @Bean
    public SqlMetricsRunner sqlMetricsRunner() {
        return new SqlMetricsRunner();
    }

    @Bean
    public BeanPostProcessor dataSourceProxyBeanPostProcessor(SqlQueryExecutionListener listener) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                if (!(bean instanceof DataSource) || !"dataSource".equals(beanName)) {
                    return bean;
                }
                return ProxyDataSourceBuilder.create((DataSource) bean)
                    .name("case001-data-source")
                    .listener(listener)
                    .build();
            }
        };
    }
}
