package com.stonesoupprogramming.wedding.repositories

import com.stonesoupprogramming.wedding.entities.RoleEntity
import com.stonesoupprogramming.wedding.entities.SiteUserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface RoleRepository : JpaRepository<RoleEntity, Long>{
    fun countByRole(role : String) : Long
}

interface  SiteUserRepository : JpaRepository<SiteUserEntity, Long> {

    fun getByUserName(userName : String) : SiteUserEntity
}