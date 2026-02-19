package com.vsp.endpointinsightsapi.aspects;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vsp.endpointinsightsapi.model.log_entry.Log;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Aspect
@RequiredArgsConstructor
public class LogAspect {

    private static final Logger LOG = LoggerFactory.getLogger(LogAspect.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpServletRequest httpRequest;

    @AfterReturning(pointcut = "execution(* com.vsp.endpointinsightsapi.service.*.*(..))", returning = "result")
    public void logSuccess(JoinPoint joinPoint, Object result) {
        Log log = new Log();
        log.setEventType(httpRequest.getMethod());
        log.setStatus("SUCCESS");

        if(!joinPoint.getSignature().getName().equals("GET")){
            try {
                Map<String, Object> json = new HashMap<>();
                json.put("request", Map.of(
                        "method", joinPoint.getSignature().getName(),
                        "uri", httpRequest.getRequestURI(),
                        "params", httpRequest.getParameterMap()
                ));

                if (result instanceof ResponseEntity<?> response) {
                    json.put("response", Map.of(
                            "status", response.getStatusCode().value()
                    ));
                } else {
                    json.put("response", result != null ? result.toString() : "null");
                }

                log.setDetails(objectMapper.writeValueAsString(json));
                LOG.info(log.toString());
            }
            catch (JsonProcessingException e) {
                LOG.error(e.getMessage());
                log.setDetails("{\"error\": \"failed to serialize\"}");
            }
        }

    }

    @AfterThrowing(pointcut = "execution(* com.vsp.endpointinsightsapi.service.*.*(..))", throwing = "ex")
    public void logFailure(JoinPoint joinPoint, Exception ex) {
        Log log = new Log();
        log.setEventType(httpRequest.getMethod());

        log.setStatus("FAILED");

        try {
            Map<String, Object> json = new HashMap<>();
            json.put("request", joinPoint.getArgs());
            json.put("response", Map.of(
                    "exception", ex.getClass().getSimpleName(),
                    "message", ex.getMessage()
            ));
            log.setDetails(objectMapper.writeValueAsString(json));
            LOG.warn(log.toString());
        }
        catch (JsonProcessingException e) {
            LOG.error(e.getMessage());
            log.setDetails("{\"error\": \"failed to serialize\"}");
        }


    }

}
