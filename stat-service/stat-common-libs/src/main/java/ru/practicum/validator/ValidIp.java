package ru.practicum.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = IpAddressValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidIp {
    String message() default "IP-адрес должен быть в формате IPv4";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
