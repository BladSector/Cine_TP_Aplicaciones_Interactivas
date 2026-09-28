package com.tp.cine;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final AdminWriteInterceptor adminWriteInterceptor;
    private final AuditoriaInterceptor auditoriaInterceptor;

    public WebConfig(AdminWriteInterceptor adminWriteInterceptor,
                     AuditoriaInterceptor auditoriaInterceptor) {
        this.adminWriteInterceptor = adminWriteInterceptor;
        this.auditoriaInterceptor = auditoriaInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(auditoriaInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/",
                        "/index.html",
                        "/styles.css",
                        "/app.js",
                        "/images/**",
                        "/error"
                )
                .order(0);

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
                )
                .order(1);
    }
}
