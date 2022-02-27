package com.simon.permissionannotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface Permission {
    PermissionEnum level() default PermissionEnum.KID;
}
