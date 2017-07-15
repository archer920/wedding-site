package com.stonesoupprogramming.wedding.entities

import com.stonesoupprogramming.wedding.validation.ValidPassword
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.hibernate.validator.constraints.Email
import org.hibernate.validator.constraints.NotBlank
import org.hibernate.validator.constraints.NotEmpty
import org.hibernate.validator.constraints.URL
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import javax.annotation.Nonnull
import javax.persistence.*
import javax.validation.constraints.NotNull
import javax.xml.bind.DatatypeConverter

@Entity
@Table(uniqueConstraints = arrayOf(UniqueConstraint(columnNames = arrayOf("role"))))
data class RoleEntity(
        @field: Id
        @field: GeneratedValue
        var id : Long = 0,

        @get: NotBlank(message = "{role.name.required}")
        var role : String = "",

        @field: ManyToMany(targetEntity = SiteUserEntity::class, cascade = arrayOf(CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH))
        var siteUserEntities: MutableSet<SiteUserEntity> = mutableSetOf())

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
        var password: String = "",

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

        @field: ManyToMany(fetch = FetchType.EAGER, cascade = arrayOf(CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH))
        @field: JoinTable(name = "user_roles",
                joinColumns = arrayOf(JoinColumn(name="user_id", updatable = true)),
                inverseJoinColumns = arrayOf(JoinColumn(name = "role_id", updatable=true)))
        var roles : MutableSet<RoleEntity> = mutableSetOf(),

        @field: Transient
        @field: NotEmpty(message = "{user.roles.empty}")
        var roleIds: Array<Long> = emptyArray()) {

    fun toUser() : User {
        val grantedAuthorities = mutableSetOf<GrantedAuthority>()
        roles.forEach { grantedAuthorities.add(SimpleGrantedAuthority(it.role)) }
        return User(userName, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, grantedAuthorities)
    }

    fun passwordMatch() : Boolean = password == validatePassword

    fun rolesString() : String = roles.joinToString(transform = { it.role })

    override fun equals(other: Any?): Boolean =
        EqualsBuilder.reflectionEquals(this, other)

    override fun hashCode(): Int =
            HashCodeBuilder.reflectionHashCode(this)
}

@Entity
@Table(uniqueConstraints = arrayOf(UniqueConstraint(columnNames = arrayOf("hash"))))
data class PersistedFileEntity(
        @field: Id @field: GeneratedValue
        var id : Long = 0,

        @field: NotBlank(message = "{persisted.file.name}")
        var fileName : String = "",

        @field: NotBlank(message = "{persisted.file.mime}")
        var mime : String = "",

        @field: NotNull(message = "{persisted.file.size}")
        var size : Long = 0,

        @field: NotNull(message = "{persisted.file.hash}")
        var hash: Int? = null,

        @field: Nonnull
        @field: Lob
        var bytes : ByteArray? = null) {

    override fun equals(other: Any?): Boolean =
        EqualsBuilder.reflectionEquals(this, other)

    override fun hashCode(): Int =
            HashCodeBuilder.reflectionHashCode(this)

    fun asBase64() : String {
        val base64 = DatatypeConverter.printBase64Binary(bytes)
        return "data:$mime;base64,$base64"
    }
}

@Entity
@Table(uniqueConstraints = arrayOf(UniqueConstraint(columnNames = arrayOf("title", "destinationLink", "displayOrder"))))
data class CarouselEntity (
        @field: Id @field: GeneratedValue
        var id : Long = 0,

        @field: NotNull(message = "{carousel.image.required}")
        @field: OneToOne(targetEntity = PersistedFileEntity::class, fetch = FetchType.EAGER, cascade = arrayOf(CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH))
        var image : PersistedFileEntity = PersistedFileEntity(),

        @field: NotBlank(message = "{carousel.title.required}")
        var title : String = "",

        @field: NotBlank(message = "{carousel.destination.required}")
        var destinationLink: String = "",

        @field: NotNull(message = "carousel.order.required")
        var displayOrder: Int = 0,

        @field: Transient
        @field: NotNull(message = "{carousel.image.required}")
        var selectedImageId : Long = 0
)