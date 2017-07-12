package com.stonesoupprogramming.wedding.entities

import com.stonesoupprogramming.wedding.validation.ValidPassword
import org.hibernate.validator.constraints.Email
import org.hibernate.validator.constraints.NotBlank
import org.hibernate.validator.constraints.NotEmpty
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

        @field: NotBlank(message = "{user.username.blank}")
        var userName: String = "",

        @field: NotBlank (message = "{user.email.blank}")
        @field: Email
        var email: String = "",

        @field: NotBlank(message = "{user.password.blank}")
        @field: ValidPassword (message = "{user.bad.password}")
        var currentPassword: String = "",

        @field: Transient
        @field: NotBlank(message = "{user.password.blank}")
        @field: ValidPassword (message = "{user.bad.password}")
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
        var roles : MutableSet<RoleEntity> = mutableSetOf(),

        @field: Transient
        @field: NotEmpty(message = "{user.roles.empty}")
        var roleIds: Array<Long> = emptyArray()) {

    fun toUser() : User {
        val grantedAuthorities = mutableSetOf<GrantedAuthority>()
        roles.forEach { grantedAuthorities.add(SimpleGrantedAuthority(it.role)) }
        return User(userName, currentPassword, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, grantedAuthorities)
    }

    fun passwordMatch() : Boolean = currentPassword == validatePassword
}