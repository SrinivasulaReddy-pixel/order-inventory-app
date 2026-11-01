package com.orderinventory.gateway;

import com.orderinventory.common.security.JwtUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;

// NOTE: deliberately NOT @ComponentScan("com.orderinventory") here - that package also
// contains JwtAuthFilter, which is a servlet-based filter. This gateway is WebFlux
// (reactive), so we cherry-pick only the plain JwtUtil class with @Import instead.
@SpringBootApplication
@EnableDiscoveryClient
@Import(JwtUtil.class)
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
