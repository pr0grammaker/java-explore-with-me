package ru.practicum.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class IpAddressValidator implements ConstraintValidator<ValidIp, String> {

    private static final String IPV4_PATTERN =
            "^(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\."
            + "(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\."
            + "(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\."
            + "(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)$";

    private static final Pattern PATTERN = Pattern.compile(IPV4_PATTERN);

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return PATTERN.matcher(value).matches();
    }
}