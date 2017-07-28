package com.stonesoupprogramming.wedding.services

import com.stonesoupprogramming.wedding.entities.*
import com.stonesoupprogramming.wedding.repositories.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDate
import javax.transaction.Transactional

interface BulkDeleteService {
    fun deleteAll(ids : LongArray) :  Int
}

@Service
@Transactional
class UserRoleService(@Autowired private val userRoleRepository: UserRoleRepository)
    : UserRoleRepository by userRoleRepository, BulkDeleteService {

    override fun <S : UserRole?> save(entity: S): S {
        entity?.role = entity?.role?.toUpperCase() ?: ""
        return userRoleRepository.save(entity)
    }

    override fun <S : UserRole?> save(entities: MutableIterable<S>?): MutableList<S> {
        entities?.forEach { it?.role = it?.role?.toUpperCase() ?: "" }
        return userRoleRepository.save(entities)
    }

    override fun <S : UserRole?> saveAndFlush(entity: S): S {
        entity?.role = entity?.role?.toUpperCase() ?: ""
        return userRoleRepository.saveAndFlush(entity)
    }

    fun findAll(ids : LongArray): MutableList<UserRole> {
        return findAll(ids.toMutableList())
    }
}

@Service
@Transactional
class SiteUserService(@Autowired val siteUserRepository: SiteUserRepository) :
        UserDetailsService, SiteUserRepository by siteUserRepository, BulkDeleteService {

    override fun loadUserByUsername(user: String): UserDetails =
            siteUserRepository.getByUserName(user).toUser()

    override fun <S : SiteUser?> save(userEntity: S): S {
        userEntity?.password = BCryptPasswordEncoder().encode(userEntity?.password)
        return siteUserRepository.save(userEntity)
    }
}

@Service
@Transactional
class IndexCarouselService(
        @Autowired private val indexCarouselRepository: IndexCarouselRepository) :
        IndexCarouselRepository by indexCarouselRepository, BulkDeleteService {

    override fun deleteAll(ids: LongArray) : Int {
        val entities = findAll(ids.toList())
        delete(entities)
        return ids.size
    }
}

@Service
@Transactional
class EventDateService(
        @Autowired private val eventDateRepository: EventDateRepository) :
        EventDateRepository by eventDateRepository, BulkDeleteService{

    override fun <S : EventDate?> save(entity: S): S {
        entity?.date = LocalDate.parse(entity?.dateStr)
        return eventDateRepository.save(entity)
    }

    override fun <S : EventDate?> save(entities: MutableIterable<S>?): MutableList<S> {
        entities?.forEach { it?.date = LocalDate.parse(it?.dateStr) }
        return eventDateRepository.save(entities)
    }

    override fun <S : EventDate?> saveAndFlush(entity: S): S {
        entity?.date = LocalDate.parse(entity?.dateStr)
        return eventDateRepository.saveAndFlush(entity)
    }
}

@Service
@Transactional
class WeddingVenueContentService(
        @Autowired
        private val weddingVenueContentRepository: WeddingVenueContentRepository) :
        WeddingVenueContentRepository by weddingVenueContentRepository, BulkDeleteService {

    override fun deleteAll(ids: LongArray): Int {
        val weddingVenueContent = findOrCreate()
        weddingVenueContent.images.removeIf { it.id ?: -1 in ids }
        save(weddingVenueContent)
        return ids.size
    }

    fun findOrCreate() : WeddingVenueContent =
            findAll(PageRequest(0, 1)).elementAtOrElse(0, { WeddingVenueContent()})

}

@Service
@Transactional
class WeddingThemeContentService(
        @Autowired
        private val weddingThemeContentRepository: WeddingThemeContentRepository) :
        WeddingThemeContentRepository by weddingThemeContentRepository{

    fun findOrCreate() : WeddingThemeContent =
            findAll(PageRequest(0, 1)).elementAtOrElse(0, { WeddingThemeContent() })
}

@Service
@Transactional
class FoodBarMenuService(
        @Autowired
        private val foodBarMenuRepository: FoodBarMenuRepository) :
        FoodBarMenuRepository by foodBarMenuRepository, BulkDeleteService {

    private fun FoodBarMenu.parse(){
        menuItems = menuItemsStr.split('\n').map{ FoodBarMenuItem(name = it.removeSurrounding("\n", "\n")) }.toMutableList()
    }

    override fun deleteAll(ids : LongArray) : Int {
        val entities = findAll(ids.toList())
        delete(entities)
        return ids.size
    }

    override fun <S : FoodBarMenu?> save(entity: S): S {
        entity?.parse()
        return foodBarMenuRepository.save(entity)
    }

    override fun <S : FoodBarMenu?> save(entities: MutableIterable<S>?): MutableList<S> {
        entities?.forEach { it?.parse() }
        return foodBarMenuRepository.save(entities)
    }

    override fun <S : FoodBarMenu?> saveAndFlush(entity: S): S {
        entity?.parse()
        return foodBarMenuRepository.saveAndFlush(entity)
    }
}

@Service
@Transactional
class AfterPartyContentService (
        @Autowired
        private val afterPartyContentRepository: AfterPartyContentRepository) :
        AfterPartyContentRepository by afterPartyContentRepository {

    fun findOrCreate() : AfterPartyInfo =
            findAll(PageRequest(0, 1)).elementAtOrElse(0, { AfterPartyInfo() })
}

@Service
@Transactional
class RegistryService(
        @Autowired
        private val registryRepository: RegistryRepository) :
        RegistryRepository by registryRepository, BulkDeleteService {

    override fun deleteAll(ids: LongArray): Int {
        val entities = findAll(ids.toList())
        delete(entities)
        return ids.size
    }
}
