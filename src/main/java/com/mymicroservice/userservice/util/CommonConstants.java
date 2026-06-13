package com.mymicroservice.userservice.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CommonConstants {

    public static final String INTERNAL_CALL_HEADER = "X-Internal-Call";
    public static final String SOURCE_SERVICE_HEADER = "X-Source-Service";
    public static final String GATEWAY_SERVICE_NAME = "gateway";
}
