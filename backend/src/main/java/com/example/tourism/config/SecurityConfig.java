package com.example.tourism.config;

import com.example.tourism.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
                // H2 数据库控制台需要 frame，学习阶段方便你直接看数据库。
                .headers().frameOptions().disable()
                .and()
                // 前后端分离项目通常不使用 Session，而是用 JWT 保存登录状态。
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                // 静态页面和 H2 控制台放行。
                .antMatchers("/", "/index.html", "/app.js", "/styles.css", "/h2-console/**").permitAll()
                // 登录、注册、公开路线、公开目的地、公开看板数据，不登录也能访问。
                .antMatchers("/api/auth/**", "/api/destinations/**", "/api/routes/**", "/api/dashboard/**").permitAll()
                // 管理员后台接口只有 ADMIN 角色能访问。
                .antMatchers("/api/admin/**").hasRole("ADMIN")
                // 其他接口需要登录，例如游客提交预订 /api/bookings。
                .anyRequest().authenticated()
                .and()
                // 把 JWT 过滤器插入 Spring Security，负责解析请求头里的 Bearer token。
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 用户密码保存到数据库前要加密，不能明文保存。
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}