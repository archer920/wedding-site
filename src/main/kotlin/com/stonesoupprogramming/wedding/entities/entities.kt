package com.stonesoupprogramming.wedding.entities

import com.stonesoupprogramming.wedding.validation.ValidDate
import com.stonesoupprogramming.wedding.validation.ValidPassword
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.hibernate.validator.constraints.Email
import org.hibernate.validator.constraints.NotBlank
import org.hibernate.validator.constraints.NotEmpty
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.web.multipart.MultipartFile
import java.time.Duration
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*
import javax.annotation.Nonnull
import javax.persistence.*
import javax.validation.constraints.NotNull
import javax.xml.bind.DatatypeConverter

@Entity
@Table(uniqueConstraints = arrayOf(UniqueConstraint(columnNames = arrayOf("role"))))
data class RoleEntity(
        @Id
        @GeneratedValue
        var id : Long? = null,

        @NotBlank(message = "{role.name.required}")
        var role : String = "",

        @ManyToMany(targetEntity = SiteUserEntity::class, cascade = arrayOf(CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH))
        var siteUserEntities: MutableSet<SiteUserEntity> = mutableSetOf())

@Entity
@Table(uniqueConstraints = arrayOf(UniqueConstraint(columnNames = arrayOf("userName", "email"))))
data class SiteUserEntity(
        @Id
        @GeneratedValue
        var id : Long? = null,

        @NotBlank(message = "{user.username.blank}")
        var userName: String = "",

        @NotBlank (message = "{user.email.blank}")
        @Email
        var email: String = "",

        @NotBlank(message = "{user.password.blank}")
        @ValidPassword (message = "{user.bad.password}")
        var password: String = "",

        @Transient
        @NotBlank(message = "{user.password.blank}")
        @ValidPassword (message = "{user.bad.password}")
        var validatePassword: String = "",

        @Nonnull
        var enabled : Boolean = true,

        @Nonnull
        var accountNonExpired: Boolean = true,

        @Nonnull
        var credentialsNonExpired: Boolean = true,

        @Nonnull
        var accountNonLocked : Boolean = true,

        @ManyToMany(fetch = FetchType.EAGER, cascade = arrayOf(CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH))
        @JoinTable(name = "user_roles",
                joinColumns = arrayOf(JoinColumn(name="user_id", updatable = true)),
                inverseJoinColumns = arrayOf(JoinColumn(name = "role_id", updatable=true)))
        var roles : MutableSet<RoleEntity> = mutableSetOf(),

        @Transient
        @NotEmpty(message = "{user.roles.empty}")
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
        @Id @GeneratedValue
        var id : Long? = null,

        @NotBlank(message = "{persisted.file.name}")
        var fileName : String = "",

        @NotBlank(message = "{persisted.file.mime}")
        var mime : String = "",

        @NotNull(message = "{persisted.file.size}")
        var size : Long = 0,

        @NotNull(message = "{persisted.file.hash}")
        var hash: Int? = null,

        @Nonnull @Lob
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
        @Id @GeneratedValue
        var id : Long? = null,

        @NotNull(message = "{carousel.image.required}")
        @OneToOne(targetEntity = PersistedFileEntity::class, cascade = arrayOf(CascadeType.ALL), orphanRemoval = true)
        var image : PersistedFileEntity = PersistedFileEntity(),

        @NotBlank(message = "{carousel.title.required}")
        var title : String = "",

        @NotBlank(message = "{carousel.destination.required}")
        var destinationLink: String = "",

        @NotNull(message = "carousel.order.required")
        var displayOrder: Int = 0,

        @Transient
        @NotNull(message = "{carousel.image.required}")
        var uploadedFile: MultipartFile? = null
)

enum class DateType { Wedding }

@Entity
@Table(uniqueConstraints = arrayOf(UniqueConstraint(columnNames = arrayOf("dateType"))))
data class EventDateEntity(
        @Id @GeneratedValue
        var id : Long? = null,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @NotNull(message = "{eventDate.required}")
        var date : LocalDate = LocalDate.now(),

        @ValidDate(message = "{eventDate.required}")
        var dateStr : String = "",

        @NotBlank(message = "{eventDate.title.blank}")
        var title : String = "",

        @Enumerated(EnumType.STRING)
        @NotNull(message = "{eventDate.type.required}")
        var dateType: DateType = DateType.Wedding){

    fun calcRemainingDays() : Long {
        return ChronoUnit.DAYS.between(LocalDate.now(), date)
    }
}

@Entity
data class WeddingVenueContent(
        @Id @GeneratedValue
        var id : Long? = null,

        @NotBlank
        var title: String = "",

        @NotBlank
        var description: String ="",

        @NotBlank
        @Column(length = 4000)
        var googleMaps : String ="",

        @OneToMany(targetEntity = PersistedFileEntity::class, cascade = arrayOf(CascadeType.ALL), orphanRemoval = true)
        var images : MutableList<PersistedFileEntity> = mutableListOf())

@Entity
data class WeddingThemeContent(
        @Id @GeneratedValue
        var id : Long? = null,

        @NotBlank(message = "{wedding.theme.content.header.required}")
        var aboutHeading : String = "",

        @Column(length = 4000)
        @NotBlank(message = "{wedding.theme.content.description.required}")
        var aboutDescription: String = "",

        @NotBlank(message = "{wedding.theme.content.header.required}")
        var examplesHeading : String = "",

        @NotBlank(message = "{wedding.theme.content.header.required}")
        var womenSubHeading : String = "",

        @NotBlank(message = "{wedding.theme.content.header.required}")
        var menSubHeading : String = "",

        @Column(length = 4000)
        @NotBlank(message = "{wedding.theme.content.description.required}")
        var womenDescription : String = "",

        @Column(length = 4000)
        @NotBlank(message = "{wedding.theme.content.description.required}")
        var menDescription : String = "",

        @OneToMany(targetEntity = PersistedFileEntity::class, cascade = arrayOf(CascadeType.ALL), orphanRemoval = true)
        var womenExamplePics : MutableList<PersistedFileEntity> = mutableListOf(),

        @OneToMany(targetEntity = PersistedFileEntity::class, cascade = arrayOf(CascadeType.ALL), orphanRemoval = true)
        var menExamplePics : MutableList<PersistedFileEntity> = mutableListOf(),

        @NotBlank(message = "{wedding.theme.content.header.required}")
        var themeInspirationHeading : String = "",

        @Column(length = 4000)
        @NotBlank(message = "{wedding.theme.content.description.required}")
        var themeDescription : String = "",

        @Column(length = 4000)
        @NotBlank(message = "{wedding.theme.content.youtube}")
        var youTubeLink : String = ""
)