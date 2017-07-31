package com.stonesoupprogramming.wedding.entities

import com.google.common.collect.ComparisonChain
import com.stonesoupprogramming.wedding.validation.ValidDate
import com.stonesoupprogramming.wedding.validation.ValidPassword
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.hibernate.annotations.Sort
import org.hibernate.annotations.SortNatural
import org.hibernate.validator.constraints.Email
import org.hibernate.validator.constraints.NotBlank
import org.hibernate.validator.constraints.NotEmpty
import org.hibernate.validator.constraints.URL
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

//IMPORTANT!!!
//All JSR-303 annotations need @field explicitly due to bug in Kotlin compiler. Specify all annotations explicitly
//because this may impact other annotations also
//https://youtrack.jetbrains.com/issue/KT-19289

@Entity
data class UserRole(
        @field: Id
        @field: GeneratedValue
        var id : Long? = null,

        @field: NotBlank(message = "{role.name.required}")
        @field: Column(unique = true)
        var role : String = "",

        @field: ManyToMany(targetEntity = SiteUser::class, cascade = arrayOf(CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH))
        var siteUserEntities: MutableSet<SiteUser> = mutableSetOf())

@Entity
data class SiteUser(
        @field: Id
        @field: GeneratedValue
        var id : Long? = null,

        @field: NotBlank(message = "{user.username.blank}")
        @field: Column(unique = true)
        var userName: String = "",

        @field: NotBlank (message = "{user.email.blank}")
        @field: Email
        @field: Column(unique = true)
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
        var roles : MutableSet<UserRole> = mutableSetOf(),

        @field: Transient
        @field: NotEmpty(message = "{user.roles.empty}")
        var roleIds: LongArray = longArrayOf()) {

    fun passwordMatch() : Boolean = password == validatePassword

    fun rolesString() : String = roles.joinToString(transform = { it.role })

    override fun equals(other: Any?): Boolean =
        EqualsBuilder.reflectionEquals(this, other)

    override fun hashCode(): Int =
            HashCodeBuilder.reflectionHashCode(this)
}

@Entity
data class PersistedFile(
        @field: Id
        @field: GeneratedValue
        var id : Long? = null,

        @field: NotBlank(message = "{persisted.file.name}")
        var fileName : String = "",

        @field: NotBlank(message = "{persisted.file.mime}")
        var mime : String = "",

        @field: NotNull(message = "{persisted.file.size}")
        var size : Long = 0,

        @field: NotNull(message = "{persisted.file.hash}")
        @field: Column(unique = true)
        var hash: Int? = null,

        @field: Nonnull
        @field: Lob
        var bytes : ByteArray? = null) : Comparable<PersistedFile>{

        override fun compareTo(other: PersistedFile): Int =
                ComparisonChain.start().compare(this?.id ?: 0, other?.id ?: 0).result()

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
data class IndexCarousel (
        @field: Id
        @field: GeneratedValue
        var id : Long? = null,

        @field: NotNull(message = "{carousel.image.required}")
        @field: OneToOne(targetEntity = PersistedFile::class, cascade = arrayOf(CascadeType.ALL), orphanRemoval = true, fetch = FetchType.EAGER)
        var image : PersistedFile = PersistedFile(),

        @field: NotBlank(message = "{carousel.title.required}")
        @field: Column(unique = true)
        var title : String = "",

        @field: NotBlank(message = "{carousel.destination.required}")
        @field: Column(unique = true)
        var destinationLink: String = "",

        @field: NotNull(message = "carousel.order.required")
        @field: Column(unique = true)
        var displayOrder: Int = 0,

        @field: Transient
        @field: NotNull(message = "{carousel.image.required}")
        var uploadedFile: MultipartFile? = null
)

enum class DateType { Wedding }

@Entity
data class EventDate(
        @field: Id
        @field: GeneratedValue
        var id : Long? = null,

        @field: DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @field: NotNull(message = "{eventDate.required}")
        var date : LocalDate = LocalDate.now(),

        @field: ValidDate(message = "{eventDate.required}")
        var dateStr : String = "",

        @field: NotBlank(message = "{eventDate.title.blank}")
        var title : String = "",

        @field: Enumerated(EnumType.STRING)
        @field: NotNull(message = "{eventDate.type.required}")
        @field: Column(unique = true)
        var dateType: DateType = DateType.Wedding){

    fun calcRemainingDays() : Long {
        return ChronoUnit.DAYS.between(LocalDate.now(), date)
    }
}

@Entity
data class WeddingVenueContent(
        @field: Id
        @field: GeneratedValue
        var id : Long? = null,

        @field: NotBlank(message = "{wedding.venue.title.blank}")
        var title: String = "",

        @field: NotBlank(message = "{wedding.venue.description.blank}")
        var description: String ="",

        @field: NotBlank(message = "{wedding.venue.google.maps}")
        @field: Column(length = 4000)
        var googleMaps : String ="",

        @field: OneToMany(targetEntity = PersistedFile::class, cascade = arrayOf(CascadeType.ALL), orphanRemoval = true, fetch = FetchType.EAGER)
        var images : MutableList<PersistedFile> = mutableListOf())

@Entity
data class WeddingThemeContent(
        @field: Id
        @field: GeneratedValue
        var id : Long? = null,

        @field: NotBlank(message = "{wedding.theme.content.header.required}")
        var aboutHeading : String = "",

        @field: Column(length = 4000)
        @field: NotBlank(message = "{wedding.theme.content.description.required}")
        var aboutDescription: String = "",

        @field: NotBlank(message = "{wedding.theme.content.header.required}")
        var examplesHeading : String = "",

        @field: NotBlank(message = "{wedding.theme.content.header.required}")
        var womenSubHeading : String = "",

        @field: NotBlank(message = "{wedding.theme.content.header.required}")
        var menSubHeading : String = "",

        @field: Column(length = 4000)
        @field: NotBlank(message = "{wedding.theme.content.description.required}")
        var womenDescription : String = "",

        @field: Column(length = 4000)
        @field: NotBlank(message = "{wedding.theme.content.description.required}")
        var menDescription : String = "",

        @field: OneToMany(targetEntity = PersistedFile::class, cascade = arrayOf(CascadeType.ALL), orphanRemoval = true, fetch = FetchType.EAGER)
        @field: SortNatural
        var womenExamplePics : SortedSet<PersistedFile> = TreeSet<PersistedFile>(),

        @field: OneToMany(targetEntity = PersistedFile::class, cascade = arrayOf(CascadeType.ALL), orphanRemoval = true, fetch = FetchType.EAGER)
        @field: SortNatural
        var menExamplePics : SortedSet<PersistedFile> = TreeSet<PersistedFile>(),

        @field: NotBlank(message = "{wedding.theme.content.header.required}")
        var themeInspirationHeading : String = "",

        @field: Column(length = 4000)
        @field: NotBlank(message = "{wedding.theme.content.description.required}")
        var themeDescription : String = "",

        @field: Column(length = 4000)
        @field: NotBlank(message = "{wedding.theme.content.youtube}")
        var youTubeLink : String = ""
)

@Entity
data class FoodBarMenuItem(
        @field: Id
        @field: GeneratedValue
        var id: Long? = null,

        @field: Column(unique = true)
        @field: NotBlank(message = "{foodbar.menu.item.required}")
        var name: String = ""
)

@Entity
data class FoodBarMenu(
        @field: Id
        @field: GeneratedValue
        var id : Long? = null,

        @field: NotBlank(message = "{foodbar.menu.title.required}")
        @field: Column(unique = true)
        var title: String = "",

        //This class has no meaning without the items so set the FetchType to EAGER
        @field: OneToMany(targetEntity = FoodBarMenuItem::class, fetch = FetchType.EAGER, cascade = arrayOf(CascadeType.ALL), orphanRemoval = true)
        var menuItems : MutableList<FoodBarMenuItem> = mutableListOf(),

        @field: Transient
        @field: NotBlank(message = "{foodbar.items.required}")
        var menuItemsStr : String = ""
)

@Entity
data class AfterPartyInfo(

        @field: Id
        @field: GeneratedValue
        var id : Long? = null,

        @field: Column(length = 4000)
        @field: NotBlank(message = "{after.party.info.required}")
        var information : String = ""
)

@Entity
data class Registry(
        @field: Id
        @field: GeneratedValue
        var id: Long? = null,

        @field: Column(unique = true)
        @field: NotBlank(message = "{registry.name.blank}")
        var name: String = "",

        @field: Column(unique = true)
        @field: NotBlank(message = "{registry.link.blank}")
        @field: URL(message = "{registry.link.invalid}")
        var link: String = "",

        @field: NotNull(message = "{registry.image.required}")
        @OneToOne(targetEntity = PersistedFile::class, cascade = arrayOf(CascadeType.ALL), orphanRemoval = true)
        var logoImage: PersistedFile = PersistedFile(),

        @field: Transient
        @field: NotNull(message = "{registry.image.required}")
        var uploadedFile: MultipartFile? = null)