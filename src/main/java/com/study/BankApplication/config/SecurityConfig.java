package com.study.BankApplication.config;

import com.study.BankApplication.domain.user.UserEnum;
import com.study.BankApplication.util.CustomResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {
    private final Logger log = LoggerFactory.getLogger(getClass());
    @Bean //Ioc 컴테이너에 BCryptPasswordEncoder() 객체가 등록됨 , @Configuration이 붙여진 곳에서만 등록 가능
    public BCryptPasswordEncoder passwordEncoder(){
        log.debug("디버그: BCryptPasswordEncoder 빈 등록");
        return new BCryptPasswordEncoder();
    }

    //JWT 필터 등록 필요!



    //JWT 서버를 만들것 -> Session 사용 안함
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http)throws Exception{
        log.debug("디버그: filterChain 빈 등록");
        http.headers().frameOptions().disable(); //iframe 허용 안함
        http.csrf().disable(); //enable 이면 post 작동 안함
        http.cors().configurationSource(configurationSource());

        //JSessionId를 서버쪽에서 관리 안하겠다는 뜻
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        //React, app으로 요청할 예정
        http.formLogin().disable();

        //httpBasic은 브라우저가 팝업창을 이용해서 사용자 인증을 진행함
        http.httpBasic().disable();

        //Exception 가로채기
        http.exceptionHandling().authenticationEntryPoint((request, response, authException)->{
            CustomResponseUtil.unAuthentication(response, "로그인을 진행해 주세요");
                });

        http.authorizeRequests()
                .antMatchers("/api/s/**").authenticated()
                .antMatchers("/api/admin/**").hasRole(""+UserEnum.ADMIN)
                .anyRequest().permitAll();

        return http.build();
    }

    public CorsConfigurationSource configurationSource(){
        log.debug("디버그: configurationSource cors 설정이 SecurityFilterChain에 등록");

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*"); //GET, POST, PUT, DELETE (JS 요청 허용)
        configuration.addAllowedOriginPattern("*"); //모든 IP주소 허용 (프론트앤드 IP만 허용)
        configuration.setAllowCredentials(true); //클라이언트에서 쿠키 요청 허용;

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
