package com.stonesoupprogramming.wedding.services

import com.stonesoupprogramming.wedding.entities.*
import com.stonesoupprogramming.wedding.repositories.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.Serializable
import java.time.LocalDate
import javax.transaction.Transactional
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

@Service
@Transactional
class RoleService(@Autowired private val roleRepository: RoleRepository) : RoleRepository by roleRepository {

    override fun <S : RoleEntity?> save(entity: S): S {
        entity?.role = entity?.role?.toUpperCase() ?: ""
        return roleRepository.save(entity)
    }

    override fun <S : RoleEntity?> save(entities: MutableIterable<S>?): MutableList<S> {
        entities?.forEach { it?.role = it?.role?.toUpperCase() ?: "" }
        return roleRepository.save(entities)
    }

    override fun <S : RoleEntity?> saveAndFlush(entity: S): S {
        entity?.role = entity?.role?.toUpperCase() ?: ""
        return roleRepository.saveAndFlush(entity)
    }
}

@Service
@Transactional
class SiteUserService(@Autowired val siteUserRepository: SiteUserRepository) :
        UserDetailsService, SiteUserRepository by siteUserRepository {

    override fun loadUserByUsername(user: String): UserDetails =
            siteUserRepository.getByUserName(user).toUser()

    override fun <S : SiteUserEntity?> save(userEntity: S): S {
        userEntity?.password = BCryptPasswordEncoder().encode(userEntity?.password)
        return siteUserRepository.save(userEntity)
    }
}

@Service
@Transactional
class PersistedFileService(@Autowired val persistedFileRepository: PersistedFileRepository) :
        PersistedFileRepository by persistedFileRepository{

    fun save(multipartFile: MultipartFile){
        val persistedFileEntity = PersistedFileEntity(fileName = multipartFile.name,
                mime = multipartFile.contentType, bytes = multipartFile.bytes, size = multipartFile.size)
        persistedFileEntity.hash = persistedFileEntity.hashCode()
        save(persistedFileEntity)
    }
}

@Service
@Transactional
class CarouselService(
        @Autowired private val carouselRepository: CarouselRepository) :
        CarouselRepository by carouselRepository

@Service
@Transactional
class EventDateService(
        @Autowired private val eventDateRepository: EventDateRepository) :
        EventDateRepository by eventDateRepository{

    override fun <S : EventDateEntity?> save(entity: S): S {
        entity?.date = LocalDate.parse(entity?.dateStr)
        return eventDateRepository.save(entity)
    }

    override fun <S : EventDateEntity?> save(entities: MutableIterable<S>?): MutableList<S> {
        entities?.forEach { it?.date = LocalDate.parse(it?.dateStr) }
        return eventDateRepository.save(entities)
    }

    override fun <S : EventDateEntity?> saveAndFlush(entity: S): S {
        entity?.date = LocalDate.parse(entity?.dateStr)
        return eventDateRepository.saveAndFlush(entity)
    }
}

@Service
@Transactional
class WeddingVenueContentService(
        @Autowired
        private val weddingVenueContentRepository: WeddingVenueContentRepository) :
        WeddingVenueContentRepository by weddingVenueContentRepository  {

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
            findAll(PageRequest(0, 1)).elementAtOrElse(0, { WeddingThemeContent()})
}