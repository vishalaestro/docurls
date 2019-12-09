// 
// Decompiled by Procyon v0.5.36
// 

package com.mxproxy;

import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import java.util.Enumeration;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestMapping;
import java.net.URI;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.util.MultiValueMap;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin(origins = { "*" }, allowedHeaders = { "*" })
@RestController
public class ProxyController
{
    @Autowired
    RouteProperties RouteProperties;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    DefaultTokenServices defaultTokenServices;
    @Autowired
    PermissionValidation permissionValidation;
    
    @RequestMapping(value = { "/**" }, method = { RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.POST, RequestMethod.GET })
    public ResponseEntity<String> routeRest(@RequestBody(required = false) final String body, HttpMethod method, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final Map<String, Object> tokenData = (Map<String, Object>)this.defaultTokenServices.readAccessToken(request.getHeader("Authorization").replace("Bearer ", "")).getAdditionalInformation();
        this.permissionValidation.validateMenu((Map)tokenData);
        final URI uri = this.constructUri(request);
        final HttpHeaders headers = new HttpHeaders();
        this.constructHeaders(request, tokenData, headers);
        final HttpEntity<String> httpEntity = (HttpEntity<String>)new HttpEntity((Object)body, (MultiValueMap)headers);
        try {
            System.out.println("final url : " + uri.toString());
            return (ResponseEntity<String>)this.restTemplate.exchange(uri, method, (HttpEntity)httpEntity, (Class)String.class);
        }
        catch (HttpStatusCodeException e) {
            return (ResponseEntity<String>)((ResponseEntity.BodyBuilder)ResponseEntity.status(e.getRawStatusCode()).headers(e.getResponseHeaders())).body((Object)e.getResponseBodyAsString());
        }
    }
    
    private URI constructUri(final HttpServletRequest request) throws Exception {
        final String requestPath = request.getRequestURI();
        System.out.println("requestPath : " + requestPath);
        String formattedRequestPath = request.getRequestURI();
        formattedRequestPath = formattedRequestPath.replace(request.getContextPath(), "");
        System.out.println("formattedRequestPath : " + formattedRequestPath);
        final String[] requestPathArray = requestPath.split("/");
        if (requestPathArray.length < 2) {
            throw new MxProxyException("Invalid Request URI format " + requestPath);
        }
        final String endPointContextPath = requestPathArray[2];
        System.out.println("endPointContextPath : " + endPointContextPath);
        final String mappedRoute = this.RouteProperties.getRoutes().get(endPointContextPath);
        if (mappedRoute == null || mappedRoute.isEmpty()) {
            throw new MxProxyException("No route mapped for " + endPointContextPath);
        }
        final URI uri = UriComponentsBuilder.fromHttpUrl((String)this.RouteProperties.getRoutes().get(endPointContextPath)).path(formattedRequestPath).query(request.getQueryString()).build(true).toUri();
        return uri;
    }
    
    private void constructHeaders(final HttpServletRequest request, final Map<String, Object> tokenData, final HttpHeaders headers) throws Exception {
        final Enumeration<String> headerNames = (Enumeration<String>)request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            final String headerName = headerNames.nextElement();
            headers.set(headerName, request.getHeader(headerName));
        }
        final JsonParser parser = new JsonParser();
        final String json = new Gson().toJson(tokenData.get("user"));
        final JsonElement user = parser.parse(json);
        final JsonObject userObject = user.getAsJsonObject();
        headers.set("merchantKeyword", userObject.get("merchantKey").getAsString());
        headers.set("mxUserId", userObject.get("userId").getAsString());
        headers.set("mxUserName", userObject.get("userName").getAsString());
        headers.set("mxGroupId", userObject.get("groupId").getAsString());
    }
}
