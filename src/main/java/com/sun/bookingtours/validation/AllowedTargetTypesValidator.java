package com.sun.bookingtours.validation;

import com.sun.bookingtours.entity.enums.TargetType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.Set;

public class AllowedTargetTypesValidator implements ConstraintValidator<AllowedTargetTypes, TargetType> {

    private Set<TargetType> allowed;

    @Override
    public void initialize(AllowedTargetTypes annotation) {
        allowed = Set.copyOf(Arrays.asList(annotation.value()));
    }

    @Override
    public boolean isValid(TargetType value, ConstraintValidatorContext context) {
        // null để @NotNull xử lý riêng
        return value == null || allowed.contains(value);
    }
}
