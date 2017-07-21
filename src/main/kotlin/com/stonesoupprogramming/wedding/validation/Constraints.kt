package com.stonesoupprogramming.wedding.validation

import org.passay.*
import java.time.LocalDate
import java.time.LocalDateTime
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import kotlin.reflect.KClass


@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = arrayOf(ValidPasswordImpl::class))
annotation class ValidPassword(
        val message: String = "{password.invalid}",
        val groups: Array<KClass<*>> = arrayOf(),
        val payload: Array<KClass<out Payload>> = arrayOf())

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = arrayOf(ValidDateImpl::class))
annotation class ValidDate(
        val message: String = "",
        val groups: Array<KClass<*>> = arrayOf(),
        val payload: Array<KClass<out Payload>> = arrayOf())

class ValidPasswordImpl : ConstraintValidator<ValidPassword, String>{

    override fun isValid(password: String?, p1: ConstraintValidatorContext?): Boolean {
        val validator = PasswordValidator(
                listOf(
                        LengthRule(8, 256),
                        CharacterRule(EnglishCharacterData.UpperCase, 1),
                        CharacterRule(EnglishCharacterData.LowerCase, 1),
                        CharacterRule(EnglishCharacterData.Digit, 1),
                        CharacterRule(EnglishCharacterData.Special, 1),
                        WhitespaceRule()
                ))
        return validator.validate(PasswordData(password ?: "")).isValid
    }

    override fun initialize(p0: ValidPassword?) {}
}

class ValidDateImpl : ConstraintValidator<ValidDate, String>{

    override fun isValid(dateStr: String?, p1: ConstraintValidatorContext?): Boolean {
        var valid = true
        try {
            LocalDate.parse(dateStr)
        } catch (e : Exception){
            valid = false
        } finally {
            return valid
        }
    }

    override fun initialize(p0: ValidDate?) {}

}