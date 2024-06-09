package org.dbsim.node.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dbsim.node.exception.InvalidTokenException;
import org.dbsim.node.model.message.ApiResponse;
import org.dbsim.node.util.GlobalVar;
import org.dbsim.node.util.security.JwtUtil;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {


    private JwtUtil jwtUtil;

    @Autowired
    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        if (requestURI.equals("/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String requestTokenHeader = request.getHeader(GlobalVar.HEADER_STRING);
            if (requestTokenHeader == null) {
                throw new InvalidTokenException("No token provided");
            }


            String token = jwtUtil.resolveToken(requestTokenHeader);
            jwtUtil.validateToken(token);

            String username = jwtUtil.getUsername(token);
            String role = jwtUtil.getRole(token);

            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority(role));
            UserDetails userDetails = new User(username, "", authorities);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);
        } catch (InvalidTokenException e) {
            ApiResponse<String> errorResponse = new ApiResponse<>(false, e.getMessage(), null);
            JSONObject responseJson = new JSONObject(new ObjectMapper().writeValueAsString(errorResponse));
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter().write(responseJson.toString());
        }
    }

}