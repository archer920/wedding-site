package com.stonesoupprogramming.wedding.entities

import com.stonesoupprogramming.wedding.validation.ValidPassword
import org.hibernate.validator.constraints.Email
import org.hibernate.validator.constraints.NotBlank
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import javax.annotation.Nonnull
import javax.persistence.*

@Entity
@Table(uniqueConstraints = arrayOf(UniqueConstraint(columnNames = arrayOf("role"))))
data class RoleEntity(
        @field: Id
        @field: GeneratedValue
        var id : Long = 0,

        @get: NotBlank(message = "{role.name.required}")
        var role : String = "",

        @field: ManyToOne(targetEntity = SiteUserEntity::class, fetch = FetchType.EAGER)
        var siteUserEntity: SiteUserEntity? = null)

@Entity
@Table(uniqueConstraints = arrayOf(UniqueConstraint(columnNames = arrayOf("userName", "email"))))
data class SiteUserEntity(
        @field: Id
        @field: GeneratedValue
        var id : Long = 0,

        @field: NotBlank
        var userName: String = "",

        @field: NotBlank
        @field: Email
        var email: String = "",

        @field: NotBlank(message = "{password.blank}")
        @field: ValidPassword
        var currentPassword: String = "",

        @field: Transient
        var validatePassword: String = "",

        @field: Nonnull
        var enabled : Boolean = true,

        @field: Nonnull
        var accountNonExpired: Boolean = true,

        @field: Nonnull
        var credentialsNonExpired: Boolean = true,

        @field: Nonnull
        var accountNonLocked : Boolean = true,

        @field: OneToMany(targetEntity = RoleEntity::class, fetch = FetchType.EAGER)
        var roles : MutableSet<RoleEntity> = mutableSetOf()) {

    fun toUser() : User {
        val grantedAuthorities = mutableSetOf<GrantedAuthority>()
        roles.forEach { grantedAuthorities.add(SimpleGrantedAuthority(it.role)) }
        return User(userName, currentPassword, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, grantedAuthorities)
    }

    fun passwordMatch() : Boolean = currentPassword == validatePassword
}