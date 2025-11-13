package com.vsp.endpointinsightsapi.authentication;

import com.vsp.endpointinsightsapi.model.enums.UserRole;

public @interface RequiredRoles {

	UserRole[] roles() default {};

	RoleAccessConfigurationType roleAccessConfigurationType() default RoleAccessConfigurationType.ONE;

}
