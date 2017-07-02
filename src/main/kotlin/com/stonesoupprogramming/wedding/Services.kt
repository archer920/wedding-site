package com.stonesoupprogramming.wedding

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import javax.transaction.Transactional

/**
 * Created by stonesoup on 7/2/17.
 */

fun UserFromWeddingUser(user: WeddingUser) : User{
    val authorities = mutableSetOf<GrantedAuthority>()
    user.roles.forEach { authorities.add(SimpleGrantedAuthority(it.role)) }
    return User(user.userName, user.password, true, true, true, true, authorities)
}

@Service
@Transactional
class UserService (@Autowired val userDao: UserDao) : UserDetailsService {
    override fun loadUserByUsername(userName: String): UserDetails {
        return UserFromWeddingUser(userDao.findByUserName(userName))
    }

    fun saveUser(weddingUser: WeddingUser) {
        userDao.addUser(weddingUser)
    }
}