// 
// Decompiled by Procyon v0.5.36
// 

package com.mxproxy;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Configuration;

@Configuration
@PropertySource({ "classpath:route.properties" })
@ConfigurationProperties(prefix = "service")
public class RouteProperties
{
    private Map<String, String> routes;
    
    public Map<String, String> getRoutes() {
        return this.routes;
    }
    
    public void setRoutes(final Map<String, String> routes) {
        this.routes = routes;
    }
}
