package com.vsp.endpointinsightsapi.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

class CustomExceptionBuilderTest {

    @Test
    void constructor_noArgs_createsBuilder() {
        CustomExceptionBuilder builder = new CustomExceptionBuilder();
        assertNotNull(builder);
    }

    @Test
    void constructor_withDetails_setsDetails() {
        CustomExceptionBuilder builder = new CustomExceptionBuilder("detail1", "detail2");
        CustomException exception = builder.withStatus(HttpStatus.BAD_REQUEST).build();
        
        assertNotNull(exception.getErrorResponse().getDetails());
        assertEquals(2, exception.getErrorResponse().getDetails().size());
        assertTrue(exception.getErrorResponse().getDetails().contains("detail1"));
        assertTrue(exception.getErrorResponse().getDetails().contains("detail2"));
    }

    @Test
    void constructor_withSingleDetail_setsDetail() {
        CustomExceptionBuilder builder = new CustomExceptionBuilder("single detail");
        CustomException exception = builder.withStatus(HttpStatus.BAD_REQUEST).build();
        
        assertNotNull(exception.getErrorResponse().getDetails());
        assertEquals(1, exception.getErrorResponse().getDetails().size());
        assertTrue(exception.getErrorResponse().getDetails().contains("single detail"));
    }

    @Test
    void withError_setsError() {
        CustomExceptionBuilder builder = new CustomExceptionBuilder();
        CustomException exception = builder
                .withError("TEST_ERROR")
                .withStatus(HttpStatus.BAD_REQUEST)
                .build();
        
        assertEquals("TEST_ERROR", exception.getErrorResponse().getError());
    }

    @Test
    void withDescription_setsDescription() {
        CustomExceptionBuilder builder = new CustomExceptionBuilder();
        CustomException exception = builder
                .withDescription("Test description")
                .withStatus(HttpStatus.BAD_REQUEST)
                .build();
        
        assertEquals("Test description", exception.getErrorResponse().getDescription());
    }

    @Test
    void withDetail_addsDetail() {
        CustomExceptionBuilder builder = new CustomExceptionBuilder();
        CustomException exception = builder
                .withDetail("detail1")
                .withDetail("detail2")
                .withStatus(HttpStatus.BAD_REQUEST)
                .build();
        
        assertNotNull(exception.getErrorResponse().getDetails());
        assertEquals(2, exception.getErrorResponse().getDetails().size());
        assertTrue(exception.getErrorResponse().getDetails().contains("detail1"));
        assertTrue(exception.getErrorResponse().getDetails().contains("detail2"));
    }

    @Test
    void withDetails_addsMultipleDetails() {
        CustomExceptionBuilder builder = new CustomExceptionBuilder();
        CustomException exception = builder
                .withDetails("detail1", "detail2", "detail3")
                .withStatus(HttpStatus.BAD_REQUEST)
                .build();
        
        assertNotNull(exception.getErrorResponse().getDetails());
        assertEquals(3, exception.getErrorResponse().getDetails().size());
    }

    @Test
    void withStatus_setsHttpStatus() {
        CustomExceptionBuilder builder = new CustomExceptionBuilder();
        CustomException exception = builder
                .withStatus(HttpStatus.NOT_FOUND)
                .build();
        
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    }

    @Test
    void build_chainedCalls_buildsCompleteException() {
        CustomException exception = new CustomExceptionBuilder()
                .withError("VALIDATION_ERROR")
                .withDescription("Validation failed")
                .withDetail("Field 'name' is required")
                .withDetail("Field 'email' is invalid")
                .withStatus(HttpStatus.BAD_REQUEST)
                .build();
        
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertEquals("VALIDATION_ERROR", exception.getErrorResponse().getError());
        assertEquals("Validation failed", exception.getErrorResponse().getDescription());
        assertEquals(2, exception.getErrorResponse().getDetails().size());
    }

    @Test
    void build_returnsCustomException() {
        CustomExceptionBuilder builder = new CustomExceptionBuilder();
        CustomException exception = builder
                .withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
        
        assertNotNull(exception);
        assertInstanceOf(CustomException.class, exception);
    }
}
