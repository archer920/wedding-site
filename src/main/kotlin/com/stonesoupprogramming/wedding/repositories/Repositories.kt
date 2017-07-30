package com.stonesoupprogramming.wedding.repositories

import com.stonesoupprogramming.wedding.entities.*
import org.springframework.data.domain.Example
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.security.access.annotation.Secured
import org.springframework.security.access.prepost.PostFilter
import org.springframework.security.access.prepost.PreAuthorize


interface UserRoleRepository : JpaRepository<UserRole, Long>{

    @Secured("ROLE_ADMIN")
    @Modifying
    @Query("DELETE FROM UserRole re where re.id in (?1)")
    fun deleteAll(ids : LongArray) : Int

    @Secured("ROLE_ADMIN")
    override fun deleteAll()

    @Secured("ROLE_ADMIN")
    override fun delete(p0: MutableIterable<UserRole>?)

    @Secured("ROLE_ADMIN")
    override fun findAll(p0: Pageable?): Page<UserRole>

    @Secured("ROLE_ADMIN")
    override fun findAll(p0: MutableIterable<Long>?): MutableList<UserRole>

    @Secured("ROLE_ADMIN")
    override fun findAll(p0: Sort?): MutableList<UserRole>

    @Secured("ROLE_ADMIN", "ROLE_USER")
    @PostFilter("hasRole('ROLE_ADMIN')")
    override fun findAll(): MutableList<UserRole>

    @Secured("ROLE_ADMIN")
    override fun deleteAllInBatch()

    @Secured("ROLE_ADMIN")
    override fun flush()

    @Secured("ROLE_ADMIN")
    override fun deleteInBatch(p0: MutableIterable<UserRole>?)

    @Secured("ROLE_ADMIN")
    override fun count(): Long

    @Secured("ROLE_ADMIN")
    override fun delete(p0: Long?)

    @Secured("ROLE_ADMIN")
    override fun delete(p0: UserRole?)

    @Secured("ROLE_ADMIN")
    override fun <S : UserRole?> findAll(p0: Example<S>?): MutableList<S>

    @Secured("ROLE_ADMIN")
    override fun <S : UserRole?> findAll(p0: Example<S>?, p1: Pageable?): Page<S>

    @Secured("ROLE_ADMIN")
    override fun <S : UserRole?> findAll(p0: Example<S>?, p1: Sort?): MutableList<S>

    @Secured("ROLE_ADMIN")
    override fun <S : UserRole?> count(p0: Example<S>?): Long

    @Secured("ROLE_ADMIN")
    override fun <S : UserRole?> save(p0: MutableIterable<S>?): MutableList<S>

    @Secured("ROLE_ADMIN")
    override fun <S : UserRole?> save(p0: S): S

    @Secured("ROLE_ADMIN")
    override fun getOne(p0: Long?): UserRole

    @Secured("ROLE_ADMIN")
    override fun <S : UserRole?> findOne(p0: Example<S>?): S

    @Secured("ROLE_ADMIN")
    override fun findOne(p0: Long?): UserRole

    @Secured("ROLE_ADMIN")
    override fun exists(p0: Long?): Boolean

    @Secured("ROLE_ADMIN")
    override fun <S : UserRole?> exists(p0: Example<S>?): Boolean

    @Secured("ROLE_ADMIN")
    override fun <S : UserRole?> saveAndFlush(p0: S): S
}

interface SiteUserRepository : JpaRepository<SiteUser, Long> {

    //Needs to be publicly accessible logging into the website
    fun getByUserName(userName : String) : SiteUser?

    @Secured("ROLE_ADMIN")
    @Modifying
    @Query("DELETE FROM SiteUser se where se.id in (?1)")
    fun deleteAll(ids : LongArray) : Int

    @Secured("ROLE_ADMIN")
    fun countByUserName(userName: String) : Long

    @Secured("ROLE_ADMIN")
    fun countByEmail(email : String) : Long

    @Secured("ROLE_ADMIN")
    override fun deleteAll()

    @Secured("ROLE_ADMIN")
    override fun flush()

    @Secured("ROLE_ADMIN")
    override fun deleteInBatch(p0: MutableIterable<SiteUser>?)

    @Secured("ROLE_ADMIN")
    override fun findAll(p0: Sort?): MutableList<SiteUser>

    @Secured("ROLE_ADMIN")
    override fun <S : SiteUser?> findAll(p0: Example<S>?, p1: Pageable?): Page<S>

    @Secured("ROLE_ADMIN")
    override fun findAll(p0: Pageable?): Page<SiteUser>

    @Secured("ROLE_ADMIN", "ROLE_USER")
    @PostFilter("hasRole('ROLE_ADMIN')")
    override fun findAll(): MutableList<SiteUser>

    @Secured("ROLE_ADMIN")
    override fun <S : SiteUser?> findAll(p0: Example<S>?, p1: Sort?): MutableList<S>

    @Secured("ROLE_ADMIN")
    override fun findAll(p0: MutableIterable<Long>?): MutableList<SiteUser>

    @Secured("ROLE_ADMIN")
    override fun <S : SiteUser?> findAll(p0: Example<S>?): MutableList<S>

    @Secured("ROLE_ADMIN")
    override fun <S : SiteUser?> save(p0: MutableIterable<S>?): MutableList<S>

    @Secured("ROLE_ADMIN")
    override fun <S : SiteUser?> save(p0: S): S

    @Secured("ROLE_ADMIN")
    override fun count(): Long

    @Secured("ROLE_ADMIN")
    override fun <S : SiteUser?> count(p0: Example<S>?): Long

    @Secured("ROLE_ADMIN")
    override fun delete(p0: Long?)

    @Secured("ROLE_ADMIN")
    override fun delete(p0: SiteUser?)

    @Secured("ROLE_ADMIN")
    override fun delete(p0: MutableIterable<SiteUser>?)

    @Secured("ROLE_ADMIN")
    override fun <S : SiteUser?> saveAndFlush(p0: S): S

    @Secured("ROLE_ADMIN")
    override fun deleteAllInBatch()

    @Secured("ROLE_ADMIN")
    override fun exists(p0: Long?): Boolean

    @Secured("ROLE_ADMIN")
    override fun <S : SiteUser?> exists(p0: Example<S>?): Boolean

    @Secured("ROLE_ADMIN")
    override fun <S : SiteUser?> findOne(p0: Example<S>?): S

    @Secured("ROLE_ADMIN")
    override fun findOne(p0: Long?): SiteUser

    @Secured("ROLE_ADMIN")
    override fun getOne(p0: Long?): SiteUser
}

interface IndexCarouselRepository : JpaRepository<IndexCarousel, Long>{

    @Secured("ROLE_ADMIN")
    fun countByDisplayOrder(value : Int) : Long

    @Secured("ROLE_ADMIN")
    fun countByTitle(value : String) : Long

    @Secured("ROLE_ADMIN")
    fun countByDestinationLink(value : String) : Long

    @Secured("ROLE_ADMIN")
    override fun <S : IndexCarousel?> save(p0: MutableIterable<S>?): MutableList<S>

    @Secured("ROLE_ADMIN")
    override fun <S : IndexCarousel?> save(p0: S): S

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun findAll(): MutableList<IndexCarousel>

    @Secured("ROLE_ADMIN")
    override fun <S : IndexCarousel?> findAll(p0: Example<S>?, p1: Pageable?): Page<S>

    @Secured("ROLE_ADMIN")
    override fun findAll(p0: Sort?): MutableList<IndexCarousel>

    @Secured("ROLE_ADMIN")
    override fun findAll(p0: Pageable?): Page<IndexCarousel>

    @Secured("ROLE_ADMIN")
    override fun findAll(p0: MutableIterable<Long>?): MutableList<IndexCarousel>

    @Secured("ROLE_ADMIN")
    override fun <S : IndexCarousel?> findAll(p0: Example<S>?, p1: Sort?): MutableList<S>

    @Secured("ROLE_ADMIN")
    override fun <S : IndexCarousel?> findAll(p0: Example<S>?): MutableList<S>

    @Secured("ROLE_ADMIN")
    override fun <S : IndexCarousel?> findOne(p0: Example<S>?): S

    @Secured("ROLE_ADMIN")
    override fun findOne(p0: Long?): IndexCarousel

    @Secured("ROLE_ADMIN")
    override fun flush()

    @Secured("ROLE_ADMIN")
    override fun <S : IndexCarousel?> count(p0: Example<S>?): Long

    @Secured("ROLE_ADMIN")
    override fun count(): Long

    @Secured("ROLE_ADMIN")
    override fun <S : IndexCarousel?> saveAndFlush(p0: S): S

    @Secured("ROLE_ADMIN")
    override fun deleteAll()

    @Secured("ROLE_ADMIN")
    override fun exists(p0: Long?): Boolean

    @Secured("ROLE_ADMIN")
    override fun <S : IndexCarousel?> exists(p0: Example<S>?): Boolean

    @Secured("ROLE_ADMIN")
    override fun delete(p0: MutableIterable<IndexCarousel>?)

    @Secured("ROLE_ADMIN")
    override fun delete(p0: Long?)

    @Secured("ROLE_ADMIN")
    override fun delete(p0: IndexCarousel?)

    @Secured("ROLE_ADMIN")
    override fun deleteAllInBatch()

    @Secured("ROLE_ADMIN")
    override fun getOne(p0: Long?): IndexCarousel

    @Secured("ROLE_ADMIN")
    override fun deleteInBatch(p0: MutableIterable<IndexCarousel>?)
}

interface EventDateRepository : JpaRepository<EventDate, Long>{

    @Secured("ROLE_ADMIN", "ROLE_USER")
    fun getByDateType(dateType: DateType) : EventDate?

    @Secured("ROLE_ADMIN", "ROLE_USER")
    @Modifying
    @Query("DELETE FROM EventDate ede where ede.id in (?1)")
    fun deleteAll(ids: LongArray) : Int

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun deleteAll()

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun getOne(p0: Long?): EventDate

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : EventDate?> findAll(p0: Example<S>?, p1: Sort?): MutableList<S>

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun findAll(): MutableList<EventDate>

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun findAll(p0: MutableIterable<Long>?): MutableList<EventDate>

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun findAll(p0: Sort?): MutableList<EventDate>

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun findAll(p0: Pageable?): Page<EventDate>

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : EventDate?> findAll(p0: Example<S>?): MutableList<S>

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : EventDate?> findAll(p0: Example<S>?, p1: Pageable?): Page<S>

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : EventDate?> exists(p0: Example<S>?): Boolean

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun exists(p0: Long?): Boolean

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : EventDate?> count(p0: Example<S>?): Long

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun count(): Long

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun delete(p0: Long?)

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun delete(p0: EventDate?)

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun delete(p0: MutableIterable<EventDate>?)

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun deleteInBatch(p0: MutableIterable<EventDate>?)

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : EventDate?> save(p0: MutableIterable<S>?): MutableList<S>

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : EventDate?> save(p0: S): S

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun flush()

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : EventDate?> saveAndFlush(p0: S): S

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun deleteAllInBatch()

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun findOne(p0: Long?): EventDate

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : EventDate?> findOne(p0: Example<S>?): S
}

interface WeddingVenueContentRepository : JpaRepository<WeddingVenueContent, Long>{

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun deleteInBatch(p0: MutableIterable<WeddingVenueContent>?)

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : WeddingVenueContent?> saveAndFlush(p0: S): S

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun delete(p0: Long?)

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun delete(p0: WeddingVenueContent?)

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun delete(p0: MutableIterable<WeddingVenueContent>?)

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun flush()

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun findAll(p0: Sort?): MutableList<WeddingVenueContent>

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : WeddingVenueContent?> findAll(p0: Example<S>?): MutableList<S>

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun findAll(p0: MutableIterable<Long>?): MutableList<WeddingVenueContent>

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun findAll(): MutableList<WeddingVenueContent>

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : WeddingVenueContent?> findAll(p0: Example<S>?, p1: Sort?): MutableList<S>

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun findAll(p0: Pageable?): Page<WeddingVenueContent>

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : WeddingVenueContent?> findAll(p0: Example<S>?, p1: Pageable?): Page<S>

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun findOne(p0: Long?): WeddingVenueContent

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : WeddingVenueContent?> findOne(p0: Example<S>?): S

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun exists(p0: Long?): Boolean

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : WeddingVenueContent?> exists(p0: Example<S>?): Boolean

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun getOne(p0: Long?): WeddingVenueContent

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : WeddingVenueContent?> save(p0: MutableIterable<S>?): MutableList<S>

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : WeddingVenueContent?> save(p0: S): S

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun count(): Long

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : WeddingVenueContent?> count(p0: Example<S>?): Long

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun deleteAll()

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun deleteAllInBatch()
}

interface WeddingThemeContentRepository : JpaRepository<WeddingThemeContent, Long>{

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun findAll(p0: Pageable?): Page<WeddingThemeContent>

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun findAll(p0: Sort?): MutableList<WeddingThemeContent>

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : WeddingThemeContent?> findAll(p0: Example<S>?): MutableList<S>

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun findAll(p0: MutableIterable<Long>?): MutableList<WeddingThemeContent>

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : WeddingThemeContent?> findAll(p0: Example<S>?, p1: Pageable?): Page<S>

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : WeddingThemeContent?> findAll(p0: Example<S>?, p1: Sort?): MutableList<S>

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun findAll(): MutableList<WeddingThemeContent>

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun delete(p0: MutableIterable<WeddingThemeContent>?)

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun delete(p0: WeddingThemeContent?)

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun delete(p0: Long?)

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun deleteAll()

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun count(): Long

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : WeddingThemeContent?> count(p0: Example<S>?): Long

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : WeddingThemeContent?> save(p0: S): S

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : WeddingThemeContent?> save(p0: MutableIterable<S>?): MutableList<S>

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : WeddingThemeContent?> saveAndFlush(p0: S): S

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun exists(p0: Long?): Boolean

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : WeddingThemeContent?> exists(p0: Example<S>?): Boolean

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun findOne(p0: Long?): WeddingThemeContent

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : WeddingThemeContent?> findOne(p0: Example<S>?): S

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun getOne(p0: Long?): WeddingThemeContent

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun flush()

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun deleteAllInBatch()

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun deleteInBatch(p0: MutableIterable<WeddingThemeContent>?)
}

interface FoodBarMenuRepository : JpaRepository<FoodBarMenu, Long>{

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : FoodBarMenu?> findOne(p0: Example<S>?): S

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun findOne(p0: Long?): FoodBarMenu

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : FoodBarMenu?> save(p0: MutableIterable<S>?): MutableList<S>

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : FoodBarMenu?> save(p0: S): S

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun delete(p0: MutableIterable<FoodBarMenu>?)

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun delete(p0: Long?)

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun delete(p0: FoodBarMenu?)

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun getOne(p0: Long?): FoodBarMenu

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun deleteInBatch(p0: MutableIterable<FoodBarMenu>?)

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun findAll(p0: Pageable?): Page<FoodBarMenu>

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : FoodBarMenu?> findAll(p0: Example<S>?, p1: Sort?): MutableList<S>

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun findAll(p0: MutableIterable<Long>?): MutableList<FoodBarMenu>

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : FoodBarMenu?> findAll(p0: Example<S>?, p1: Pageable?): Page<S>

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : FoodBarMenu?> findAll(p0: Example<S>?): MutableList<S>

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun findAll(): MutableList<FoodBarMenu>

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun findAll(p0: Sort?): MutableList<FoodBarMenu>

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : FoodBarMenu?> saveAndFlush(p0: S): S

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : FoodBarMenu?> count(p0: Example<S>?): Long

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun count(): Long

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun deleteAll()

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun exists(p0: Long?): Boolean

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : FoodBarMenu?> exists(p0: Example<S>?): Boolean

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun flush()

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun deleteAllInBatch()
}

interface AfterPartyContentRepository : JpaRepository<AfterPartyInfo, Long>{

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun deleteAll()

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : AfterPartyInfo?> saveAndFlush(p0: S): S

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : AfterPartyInfo?> exists(p0: Example<S>?): Boolean

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun exists(p0: Long?): Boolean

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun findAll(p0: MutableIterable<Long>?): MutableList<AfterPartyInfo>

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : AfterPartyInfo?> findAll(p0: Example<S>?, p1: Pageable?): Page<S>

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun findAll(p0: Sort?): MutableList<AfterPartyInfo>

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun findAll(): MutableList<AfterPartyInfo>

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : AfterPartyInfo?> findAll(p0: Example<S>?): MutableList<S>

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : AfterPartyInfo?> findAll(p0: Example<S>?, p1: Sort?): MutableList<S>

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun findAll(p0: Pageable?): Page<AfterPartyInfo>

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun deleteAllInBatch()

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : AfterPartyInfo?> save(p0: S): S

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : AfterPartyInfo?> save(p0: MutableIterable<S>?): MutableList<S>

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun delete(p0: MutableIterable<AfterPartyInfo>?)

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun delete(p0: Long?)

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun delete(p0: AfterPartyInfo?)

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun count(): Long

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : AfterPartyInfo?> count(p0: Example<S>?): Long

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun deleteInBatch(p0: MutableIterable<AfterPartyInfo>?)

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun findOne(p0: Long?): AfterPartyInfo

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : AfterPartyInfo?> findOne(p0: Example<S>?): S

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun flush()

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun getOne(p0: Long?): AfterPartyInfo
}

interface RegistryRepository : JpaRepository<Registry, Long>{

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun exists(p0: Long?): Boolean

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : Registry?> exists(p0: Example<S>?): Boolean

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : Registry?> saveAndFlush(p0: S): S

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : Registry?> findAll(p0: Example<S>?): MutableList<S>

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun findAll(p0: Pageable?): Page<Registry>

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : Registry?> findAll(p0: Example<S>?, p1: Sort?): MutableList<S>

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : Registry?> findAll(p0: Example<S>?, p1: Pageable?): Page<S>

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun findAll(): MutableList<Registry>

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun findAll(p0: MutableIterable<Long>?): MutableList<Registry>

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun findAll(p0: Sort?): MutableList<Registry>

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun findOne(p0: Long?): Registry

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : Registry?> findOne(p0: Example<S>?): S

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun deleteInBatch(p0: MutableIterable<Registry>?)

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun deleteAll()

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun delete(p0: Long?)

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun delete(p0: MutableIterable<Registry>?)

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun delete(p0: Registry?)

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : Registry?> save(p0: MutableIterable<S>?): MutableList<S>

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : Registry?> save(p0: S): S

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun <S : Registry?> count(p0: Example<S>?): Long

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun count(): Long

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun getOne(p0: Long?): Registry

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun deleteAllInBatch()

    @Secured("ROLE_ADMIN", "ROLE_USER")
    override fun flush()
}