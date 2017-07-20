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
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import java.util.*

@SpringBootApplication
@EnableCaching
class WeddingSiteApplication

@Configuration
class SecurityConfig
(@Autowired private val siteUserService : SiteUserService)
    : WebSecurityConfigurerAdapter(){

    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.inMemoryAuthentication().withUser("bob").password("bob").roles("ADMIN")
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
    fun logger(injectionPoint: InjectionPoint) : Logger =
            LoggerFactory.getLogger(injectionPoint.methodParameter.containingClass)

}

fun main(args: Array<String>) {
    SpringApplication.run(WeddingSiteApplication::class.java, *args)
}