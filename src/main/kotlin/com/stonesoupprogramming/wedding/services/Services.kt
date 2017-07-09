package com.stonesoupprogramming.wedding.services

import com.stonesoupprogramming.wedding.entities.SiteUserEntity
import com.stonesoupprogramming.wedding.repositories.SiteUserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class SiteUserService(@Autowired private val siteUserRepository: SiteUserRepository) : UserDetailsService {

    override fun loadUserByUsername(user: String): UserDetails =
            siteUserRepository.getByUserName(user).toUser()

    fun save(userEntity: SiteUserEntity): SiteUserEntity {
        userEntity.password = BCryptPasswordEncoder().encode(userEntity.password)
        return siteUserRepository.save(userEntity)
    }
}