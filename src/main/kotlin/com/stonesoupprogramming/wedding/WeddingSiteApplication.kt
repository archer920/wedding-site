package com.stonesoupprogramming.wedding

import com.stonesoupprogramming.wedding.services.SiteUserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import java.util.*

@SpringBootApplication
@EnableJpaRepositories
class WeddingSiteApplication

@Configuration
class SecurityConfig
(@Autowired private val siteUserService : SiteUserService)
    : WebSecurityConfigurerAdapter(){

    override fun configure(auth: AuthenticationManagerBuilder) {
//        auth.userDetailsService(siteUserService).passwordEncoder(BCryptPasswordEncoder())

        auth.inMemoryAuthentication().withUser("admin").password("admin").roles("admin")
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
}

fun main(args: Array<String>) {
    SpringApplication.run(WeddingSiteApplication::class.java, *args)
}