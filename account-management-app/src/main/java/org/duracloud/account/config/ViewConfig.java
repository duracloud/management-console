/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import org.springframework.web.servlet.view.tiles3.SpringBeanPreparerFactory;
import org.springframework.web.servlet.view.tiles3.TilesConfigurer;
import org.springframework.web.servlet.view.tiles3.TilesView;

/**
 * Configuration for Apache Tiles views
 *
 * @author mikejritter
 */
@Configuration
public class ViewConfig {

    @Bean
    public ViewResolver viewResolver() {
        final var resolver = new UrlBasedViewResolver();
        resolver.setViewClass(TilesView.class);
        resolver.setOrder(1);
        return resolver;
    }

    @Bean
    public TilesConfigurer tilesConfigurer() {
        final var configurer = new TilesConfigurer();
        configurer.setDefinitions("/WEB-INF/defs/general.xml", "/WEB-INF/defs/root.xml", "/WEB-INF/**/views.xml");
        configurer.setPreparerFactoryClass(SpringBeanPreparerFactory.class);
        return configurer;
    }

    @Bean
    public InternalResourceViewResolver jspViewResolver() {
        final var resolver = new InternalResourceViewResolver();
        resolver.setViewClass(JstlView.class);
        resolver.setPrefix("/WEB-INF/jspx/");
        resolver.setSuffix(".jspx");
        return resolver;
    }

}
