package ru.mirea.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


    @Configuration
    public class MvcConfig implements WebMvcConfigurer {
        @Value("${upload.path}")
        String uploadPath;

        @Override
        public void addViewControllers(ViewControllerRegistry registry) {
            registry.addViewController("/RegistrationPage").setViewName("RegistrationPage");
        }

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/static/**")
                    .addResourceLocations("classpath:/static/");
            registry.addResourceHandler("/avatars/**").addResourceLocations("file:" + uploadPath + "/");
        }
    }
