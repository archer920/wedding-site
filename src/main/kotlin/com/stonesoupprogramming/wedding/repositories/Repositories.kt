package com.stonesoupprogramming.wedding.repositories

import com.stonesoupprogramming.wedding.entities.CarouselEntity
import com.stonesoupprogramming.wedding.entities.PersistedFileEntity
import com.stonesoupprogramming.wedding.entities.RoleEntity
import com.stonesoupprogramming.wedding.entities.SiteUserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface RoleRepository : JpaRepository<RoleEntity, Long>{
    fun countByRole(role : String) : Long
}

interface  SiteUserRepository : JpaRepository<SiteUserEntity, Long> {

    fun getByUserName(userName : String) : SiteUserEntity

    fun countByUserName(userName : String) : Long

    fun countByEmail(email : String) : Long
}

interface PersistedFileRepository : JpaRepository<PersistedFileEntity, Long>

interface CarouselRepository : JpaRepository<CarouselEntity, Long>{

    @Modifying
    @Query("DELETE FROM CarouselEntity ce where ce.id in (?1)")
    fun deleteAll(ids : List<Long>) : Int
}