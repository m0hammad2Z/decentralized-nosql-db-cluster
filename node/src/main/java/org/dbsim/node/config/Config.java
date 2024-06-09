package org.dbsim.node.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dbsim.broadcasting.api.Communication;
import org.dbsim.broadcasting.core.Broadcaster;
import org.dbsim.broadcasting.transport.SocketCommunication;
import org.dbsim.node.filter.JwtAuthenticationFilter;
import org.dbsim.node.service.user.UserService;
import org.dbsim.node.util.GlobalVar;
import org.dbsim.node.util.security.JwtUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;


@Configuration
@EnableWebSecurity
public class Config extends WebSecurityConfigurerAdapter {
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }


    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public Communication communication() {
        int port = GlobalVar.BROADCASTING_LISTENER_PORT;
        return new SocketCommunication(port, "org.dbsim.node", 2);
    }

    @Bean
    @DependsOn("communication")
    public Broadcaster broadcaster() {
        Broadcaster broadcaster = new Broadcaster(1);
        broadcaster.addClient(GlobalVar.BOOTSTRAPPING_SERVER, GlobalVar.BOOTSTRAPPING_PORT);
        return broadcaster;
    }

    @Bean
    public JwtUtil jwtUtil() {
        return new JwtUtil();
    }

    @Autowired
    private UserService userService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable().
                authorizeRequests()
                .antMatchers("/login").permitAll()
                .antMatchers("/addUser", "/deleteUser").hasAuthority("ADMIN")
                .anyRequest().authenticated()
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil()), UsernamePasswordAuthenticationFilter.class)
                ;
    }


    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService);
    }
}