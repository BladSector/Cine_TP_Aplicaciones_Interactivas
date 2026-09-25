package com.tp.cine;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final AdminWriteInterceptor adminWriteInterceptor;

    public WebConfig(AdminWriteInterceptor adminWriteInterceptor) {
        this.adminWriteInterceptor = adminWriteInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminWriteInterceptor)
                .addPathPatterns(
                        "/categorias/**",
                        "/peliculas/**",
                        "/salas/**",
                        "/funciones/**",
                        "/butacas/**",
                        "/productos-confiteria/**",
                        "/entradas/**",
                        "/tickets/**",
                        "/items-consumo/**",
                        "/empleados/**"
                );
    }
}
