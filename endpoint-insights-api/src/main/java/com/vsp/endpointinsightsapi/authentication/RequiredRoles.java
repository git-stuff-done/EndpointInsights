package com.vsp.endpointinsightsapi.authentication;

import com.vsp.endpointinsightsapi.model.enums.UserRole;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface RequiredRoles {

	UserRole[] roles() default {};

	RoleAccessConfigurationType roleAccessConfigurationType() default RoleAccessConfigurationType.ONE;

}
