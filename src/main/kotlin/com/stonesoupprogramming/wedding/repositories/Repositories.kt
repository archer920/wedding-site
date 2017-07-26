package com.stonesoupprogramming.wedding.repositories

import com.stonesoupprogramming.wedding.entities.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface UserRoleRepository : JpaRepository<UserRole, Long>{
    fun countByRole(role : String) : Long

    @Modifying
    @Query("DELETE FROM UserRole re where re.id in (?1)")
    fun deleteAll(ids : List<Long>) : Int
}

interface SiteUserRepository : JpaRepository<SiteUser, Long> {

    fun getByUserName(userName : String) : SiteUser

    @Modifying
    @Query("DELETE FROM SiteUser se where se.id in (?1)")
    fun deleteAll(ids : List<Long>) : Int

    fun countByUserName(userName: String) : Long

    fun countByEmail(email : String) : Long
}

interface IndexCarouselRepository : JpaRepository<IndexCarousel, Long>{

    @Query("FROM IndexCarousel ce JOIN FETCH ce.image order by ce.displayOrder ASC")
    fun findAllEager() : MutableList<IndexCarousel>

    fun countByDisplayOrder(value : Int) : Long

    fun countByTitle(value : String) : Long

    fun countByDestinationLink(value : String) : Long
}

interface EventDateRepository : JpaRepository<EventDate, Long>{

    fun getByDateType(dateType: DateType) : EventDate?

    @Modifying
    @Query("DELETE FROM EventDate ede where ede.id in (?1)")
    fun deleteAll(ids: List<Long>) : Int
}

interface WeddingVenueContentRepository : JpaRepository<WeddingVenueContent, Long>

interface WeddingThemeContentRepository : JpaRepository<WeddingThemeContent, Long>

interface FoodBarMenuRepository : JpaRepository<FoodBarMenu, Long>{

    @Modifying
    @Query("DELETE FROM FoodBarMenu fbm WHERE fbm.id in (?1)")
    fun deleteAll(ids : LongArray) : Int
}

interface AfterPartyContentRepository : JpaRepository<AfterPartyInfo, Long>