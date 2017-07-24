package com.stonesoupprogramming.wedding.repositories

import com.stonesoupprogramming.wedding.entities.*
import org.springframework.data.domain.Page
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.awt.print.Pageable

interface RoleRepository : JpaRepository<RoleEntity, Long>{
    fun countByRole(role : String) : Long

    @Modifying
    @Query("DELETE FROM RoleEntity re where re.id in (?1)")
    fun deleteAll(ids : List<Long>) : Int
}

interface  SiteUserRepository : JpaRepository<SiteUserEntity, Long> {

    fun getByUserName(userName : String) : SiteUserEntity

    fun countByUserName(userName : String) : Long

    fun countByEmail(email : String) : Long

    @Modifying
    @Query("DELETE FROM SiteUserEntity se where se.id in (?1)")
    fun deleteAll(ids : List<Long>) : Int
}

interface PersistedFileRepository : JpaRepository<PersistedFileEntity, Long>{

    @Modifying
    @Query("DELETE FROM PersistedFileEntity pe where pe.id in (?1)")
    fun deleteAll(ids : List<Long>) : Int
}

interface CarouselRepository : JpaRepository<CarouselEntity, Long>{

    @Modifying
    @Query("DELETE FROM CarouselEntity ce where ce.id in (?1)")
    fun deleteAll(ids : List<Long>) : Int

    @Query("FROM CarouselEntity ce JOIN FETCH ce.image order by ce.displayOrder ASC")
    fun findAllEager() : MutableList<CarouselEntity>

    fun countByDisplayOrder(value : Int) : Long
}

interface EventDateRepository : JpaRepository<EventDateEntity, Long>{

    fun getByDateType(dateType: DateType) : EventDateEntity

    @Modifying
    @Query("DELETE FROM EventDateEntity ede where ede.id in (?1)")
    fun deleteAll(ids: List<Long>) : Int
}

interface WeddingVenueContentRepository : JpaRepository<WeddingVenueContent, Long>

interface WeddingThemeContentRepository : JpaRepository<WeddingThemeContent, Long>