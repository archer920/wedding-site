package com.stonesoupprogramming.wedding.services

import com.stonesoupprogramming.wedding.entities.PersistedFileEntity
import com.stonesoupprogramming.wedding.entities.SiteUserEntity
import com.stonesoupprogramming.wedding.repositories.PersistedFileRepository
import com.stonesoupprogramming.wedding.repositories.SiteUserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import javax.transaction.Transactional

@Service
@Transactional
class SiteUserService(@Autowired val siteUserRepository: SiteUserRepository) : UserDetailsService {

    override fun loadUserByUsername(user: String): UserDetails =
            siteUserRepository.getByUserName(user).toUser()

    fun save(userEntity: SiteUserEntity): SiteUserEntity {
        userEntity.password = BCryptPasswordEncoder().encode(userEntity.password)
        return siteUserRepository.save(userEntity)
    }

   fun deleteAll(siteUsers: List<Long>){
       val entities = siteUserRepository.findAll(siteUsers)
       siteUserRepository.delete(entities)
   }
}

@Service
@Transactional
class PersistedFileService(@Autowired val persistedFileRepository: PersistedFileRepository){

    fun save(persistedFileEntity: PersistedFileEntity)
            = persistedFileRepository.save(persistedFileEntity)

    fun save(multipartFile: MultipartFile){
        val persistedFileEntity = PersistedFileEntity(fileName = multipartFile.name,
                mime = multipartFile.contentType, bytes = multipartFile.bytes, size = multipartFile.size)
        persistedFileEntity.hash = persistedFileEntity.hashCode()
        save(persistedFileEntity)
    }

    @Cacheable(cacheNames = arrayOf("persistedFile"), key="#id")
    fun findOne(id : Long) : PersistedFileEntity = persistedFileRepository.findOne(id)

    fun findAll() = persistedFileRepository.findAll().toList()

    @CacheEvict(cacheNames = arrayOf("persistedFile"), key = "#id")
    fun delete(id : Long) = persistedFileRepository.delete(id)

    fun deleteAll(ids: LongArray) {
        ids.forEach { delete(it) }
    }
}