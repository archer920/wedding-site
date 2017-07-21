package com.stonesoupprogramming.wedding.services

import com.stonesoupprogramming.wedding.entities.PersistedFileEntity
import com.stonesoupprogramming.wedding.entities.RoleEntity
import com.stonesoupprogramming.wedding.entities.SiteUserEntity
import com.stonesoupprogramming.wedding.repositories.CarouselRepository
import com.stonesoupprogramming.wedding.repositories.PersistedFileRepository
import com.stonesoupprogramming.wedding.repositories.RoleRepository
import com.stonesoupprogramming.wedding.repositories.SiteUserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import javax.transaction.Transactional

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