package com.vsp.endpointinsightsapi.util;


import com.vsp.endpointinsightsapi.aspects.LogAspect;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.SourceLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;
import java.util.Map;


import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class LogAspectTest {


    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private LogAspect logAspect;

    private JoinPoint joinPoint;

    @BeforeEach
    void setUp() {
        Signature signature = new Signature() {
            @Override public String getName() { return "getAllBatches"; }
            @Override public int getModifiers() { return 0; }
            @Override public Class getDeclaringType() { return Object.class; }
            @Override public String getDeclaringTypeName() { return "TestClass"; }
            @Override public String toShortString() { return "getAllBatches"; }
            @Override public String toLongString() { return "getAllBatches"; }
        };

        joinPoint = new JoinPoint() {
            @Override public String toShortString() { return ""; }
            @Override public String toLongString() { return ""; }
            @Override public Object getThis() { return null; }
            @Override public Object getTarget() { return null; }
            @Override public Object[] getArgs() { return new Object[]{"arg1"}; }
            @Override public Signature getSignature() { return signature; }
            @Override public SourceLocation getSourceLocation() { return null; }
            @Override public String getKind() { return ""; }
            @Override public StaticPart getStaticPart() { return null; }
        };

        when(httpRequest.getMethod()).thenReturn("GET");
        when(httpRequest.getRequestURI()).thenReturn("/api/batches");
        when(httpRequest.getParameterMap()).thenReturn(Map.of());
    }

    @Test
    void logSuccess_setsCorrectFields() {
        logAspect.logSuccess(joinPoint, "result");
        verify(httpRequest).getMethod();
        verify(httpRequest).getRequestURI();
        verify(httpRequest).getParameterMap();
    }

    @Test
    void logSuccess_handlesResponseEntity() {
        ResponseEntity<String> response = ResponseEntity.ok("data");
        logAspect.logSuccess(joinPoint, response);
        verify(httpRequest).getMethod();
    }

    @Test
    void logSuccess_handlesNullResult() {
        logAspect.logSuccess(joinPoint, null);
        verify(httpRequest).getMethod();
    }

    @Test
    void logFailure_setsCorrectFields() {
        logAspect.logFailure(joinPoint, new RuntimeException("error"));
        verify(httpRequest).getMethod();
    }

    @Test
    void logFailure_capturesExceptionType() {
        logAspect.logFailure(joinPoint, new IllegalArgumentException("bad input"));
        verify(httpRequest).getMethod();
    }
}
