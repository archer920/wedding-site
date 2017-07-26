package com.stonesoupprogramming.wedding

import com.stonesoupprogramming.wedding.services.SiteUserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InjectionPoint
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.core.convert.converter.Converter
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect
import java.util.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.validation.MessageInterpolatorFactory
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.Role
import org.springframework.core.env.Environment
import javax.validation.Validator


@SpringBootApplication
@EnableCaching
class WeddingSiteApplication

@Configuration
class SecurityConfig
(@Autowired private val siteUserService : SiteUserService)
    : WebSecurityConfigurerAdapter(){

    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.inMemoryAuthentication().withUser("admin").password("admin").roles("ADMIN")
        //auth.userDetailsService(siteUserService).passwordEncoder(BCryptPasswordEncoder())
    }

    override fun configure(http: HttpSecurity) {
        http
                .formLogin()
                .and()
                .httpBasic()
                .and()
                .authorizeRequests()
                .antMatchers("/admin").authenticated()
                .anyRequest().permitAll()
    }
}

@Configuration
class BeanConfig {

    @Bean(name = arrayOf("ValidationProperties"))
    fun validationPropertiesBean() : Properties {
        val properties = Properties()
        properties.load(BeanConfig::class.java.getResourceAsStream("/ValidationMessages.properties"))
        return properties
    }

    @Bean
    @Scope("prototype")
    fun logger(injectionPoint: InjectionPoint?): Logger =
            LoggerFactory.getLogger(injectionPoint!!.methodParameter.containingClass)
}

fun main(args: Array<String>) {
    SpringApplication.run(WeddingSiteApplication::class.java, *args)
}