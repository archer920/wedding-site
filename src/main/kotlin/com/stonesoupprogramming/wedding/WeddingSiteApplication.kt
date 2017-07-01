package com.stonesoupprogramming.wedding

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType
import org.springframework.orm.hibernate4.LocalSessionFactoryBean
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import java.util.*
import javax.sql.DataSource

@SpringBootApplication
class WeddingSiteApplication

@Configuration
class DataConfig {

    @Bean(name = arrayOf("dataSource"))
    fun dataSource() : DataSource = EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.HSQL)
                .build()

    @Bean
    fun sessionFactory(@Qualifier("dataSource") dataSource: DataSource) : LocalSessionFactoryBean {
        val properties = Properties()
        properties.setProperty("dialect", "org.hibernate.dialect.HSQLDB")

        val sessionFactory = LocalSessionFactoryBean().apply {
            setDataSource(dataSource)
            setPackagesToScan("com.stonesoupprogramming.wedding")
        }
        sessionFactory.hibernateProperties = properties
        return sessionFactory
    }
}

class SecurityWebInitializer (@Autowired @Qualifier("dataSource") val dataSource: DataSource)
    : WebSecurityConfigurerAdapter(){

    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.jdbcAuthentication().dataSource(dataSource)
    }

    override fun configure(http: HttpSecurity) {
        super.configure(http)
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(WeddingSiteApplication::class.java, *args)
}
