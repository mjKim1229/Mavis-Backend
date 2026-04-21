package com.mavis.domain.global.config;

import com.querydsl.core.types.EntityPath;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class QueryDslInitializer implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(EntityPath.class));

        scanner.findCandidateComponents("com.mavis")
                .stream()
                .map(BeanDefinition::getBeanClassName)
                .forEach(className -> {
                    try {
                        Class.forName(className);
                        log.debug("QueryDSL Q class initialized: {}", className);
                    } catch (ClassNotFoundException e) {
                        log.warn("QueryDSL Q class not found: {}", className);
                    }
                });

        log.info("QueryDSL Q class pre-initialization complete");
    }
}
