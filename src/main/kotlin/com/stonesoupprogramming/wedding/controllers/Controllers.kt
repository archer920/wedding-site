package com.stonesoupprogramming.wedding.controllers

import com.stonesoupprogramming.wedding.entities.*
import com.stonesoupprogramming.wedding.services.*
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import java.util.*
import javax.validation.ConstraintViolationException
import javax.validation.Valid

@Controller
class MessageHandler(
        @Autowired @Qualifier("ValidationProperties")
        private var validationProperties: Properties) {

    private object MessageAttributes {
        const val ERROR_MSG = "errorMessages"
        const val INFO_MSG = "infoMessages"
    }

    private object MessageMappings {
        const val MESSAGES = "/messages"
    }

    private object MessageOutcomes {
        const val MESSAGES = "/fragments/master/alerts :: alerts"
    }

    private val errorMessages: MutableSet<String> = mutableSetOf()
    private val infoMessages: MutableSet<String> = mutableSetOf()

    private fun Model.addErrorMessages(messages: List<String> = errorMessages.toList(), clear: Boolean = true) {
        addAttribute(MessageAttributes.ERROR_MSG, messages)
        if (clear) {
            errorMessages.clear()
        }
    }

    private fun Model.addInfoMessages(messages: List<String> = infoMessages.toList(), clear: Boolean = true) {
        addAttribute(MessageAttributes.INFO_MSG, messages)
        if (clear) {
            infoMessages.clear()
        }
    }

    fun showError() {
        errorMessages.add(validationProperties.getProperty("general.server.error") ?: "")
    }

    fun showError(error: String) {
        errorMessages.add(error)
    }

    fun showInfo(info: String) {
        infoMessages.add(info)
    }

    @ModelAttribute(MessageAttributes.INFO_MSG)
    fun errorMessages(): List<String> = errorMessages.toList()

    @ModelAttribute(MessageAttributes.ERROR_MSG)
    fun infoMessages(): List<String> = infoMessages.toList()

    @GetMapping(MessageMappings.MESSAGES)
    fun fetchMessages(model: Model): String {
        with(model) {
            addErrorMessages()
            addInfoMessages()
        }

        return MessageOutcomes.MESSAGES
    }
}

@Controller
class BannerController(
        @Autowired private val indexCarouselService: IndexCarouselService,
        @Autowired private val eventDateService: EventDateService) {

    private object BannerMappings {
        const val NAV_BAR = "/banner/navbarLinks"
    }

    private object BannerOutcomes {
        const val BANNER = "fragments/master/banner :: banner"
    }

    private object BannerAttributes {
        const val DAYS_REMAINING = "daysRemaining"
        const val NAV_BAR_LINKS = "navbarLinks"
    }

    private fun Model.addNavbarLinks(links: List<IndexCarousel> = indexCarouselService.findAll()) {
        addAttribute(BannerAttributes.NAV_BAR_LINKS, links)
    }

    private fun Model.addDaysRemaining(count: Long? = eventDateService.getByDateType(DateType.Wedding)?.calcRemainingDays()) {
        if(count != null){
            addAttribute(BannerAttributes.DAYS_REMAINING, count)
        }
    }

    @ModelAttribute(BannerAttributes.DAYS_REMAINING)
    fun fetchDaysRemaining(): Long =
            eventDateService.getByDateType(DateType.Wedding)?.calcRemainingDays() ?: -1

    @ModelAttribute(BannerAttributes.NAV_BAR_LINKS)
    fun navBarLinks(): List<IndexCarousel> = indexCarouselService.findAll()

    @GetMapping(BannerMappings.NAV_BAR)
    fun refreshNavBarLinks(model: Model): String {
        with(model) {
            addNavbarLinks()
            addDaysRemaining()
        }
        return BannerOutcomes.BANNER
    }
}


@Controller
class AdminController(@Autowired private val logger: Logger,
                      @Autowired @Qualifier("ValidationProperties") private val validationProperties: Properties,
                      @Autowired private val messageHandler: MessageHandler,
                      @Autowired private val siteUserService: SiteUserService,
                      @Autowired private val userRoleService: UserRoleService,
                      @Autowired private val indexCarouselService: IndexCarouselService,
                      @Autowired private val eventDateService: EventDateService,
                      @Autowired private val weddingVenueContentService: WeddingVenueContentService,
                      @Autowired private val weddingThemeContentService: WeddingThemeContentService,
                      @Autowired private val foodBarMenuService: FoodBarMenuService,
                      @Autowired private val afterPartyContentService: AfterPartyContentService) {

    //Compile Time Constants Used in Controller
    private object AdminOutcomes {
        const val ADMIN = "admin"
        const val DELETE_ROLES = "fragments/admin/user_roles/delete_roles :: delete_roles"
        const val ADD_ROLES = "fragments/admin/user_roles/add_roles :: add_roles"
        const val DELETE_SITE_USERS = "fragments/admin/site_users/delete_site_users :: delete_site_users"
        const val ADD_SITE_USER = "fragments/admin/site_users/add_site_user :: add_site_user"
        const val ADD_INDEX_CAROUSEL = "fragments/admin/index_carousel/add_index_carousel :: add_index_carousel"
        const val DELETE_CAROUSEL = "fragments/admin/index_carousel/delete_carousel :: delete_carousel"
        const val ADD_DATE = "fragments/admin/event_dates/add_date :: add_date"
        const val DELETE_EVENT_DATE = "fragments/admin/event_dates/delete_date :: delete_event_date"
        const val EDIT_WEDDING_VENUE_TEXT = "/fragments/admin/wedding_venue/edit_wedding_venue_text :: edit_venue_text"
        const val EDIT_WEDDING_VENUE_IMAGE_UPLOAD = "/fragments/admin/wedding_venue/edit_wedding_venue_image_upload :: venue_image_upload"
        const val EDIT_WEDDING_VENUE_IMAGE_DELETE = "/fragments/admin/wedding_venue/edit_wedding_venue_image_delete :: delete_wedding_venue_images"
        const val WEDDING_THEME_CONTENT = "/fragments/admin/wedding_theme/wedding_theme_content :: wedding_theme_content"
        const val WEDDING_THEME_MEN_UPLOAD = "/fragments/admin/wedding_theme/men_picture_upload :: men_picture_upload"
        const val WEDDING_THEME_MEN_DELETE = "/fragments/admin/wedding_theme/delete_mens_pictures :: men_picture_delete"
        const val WEDDING_THEME_WOMEN_UPLOAD = "/fragments/admin/wedding_theme/women_picture_upload :: women_picture_upload"
        const val WEDDING_THEME_WOMEN_DELETE = "/fragments/admin/wedding_theme/delete_womens_pictures :: women_picture_delete"
        const val FOOD_BAR_MENU_ADD = "/fragments/admin/foodbar/add_foodbar :: food_bar_add"
        const val FOOD_BAR_MENU_DELETE = "/fragments/admin/foodbar/delete_foodbar :: food_bar_delete"
        const val EDIT_AFTER_PARTY = "/fragments/admin/after_party/edit_after_party :: edit_after_party"
    }

    private object AdminAttributes {
        const val USER_ROLE_LIST = "userRoleList"
        const val USER_ROLE = "userRole"
        const val SITE_USER_LIST = "siteUserList"
        const val SITE_USER = "siteUser"
        const val INDEX_CAROUSEL_LIST = "indexCarouselList"
        const val INDEX_CAROUSEL = "indexCarousel"
        const val EVENT_DATE = "eventDate"
        const val EVENT_DATE_LIST = "eventDateList"
        const val WEDDING_VENUE_CONTENT = "weddingVenueContent"
        const val WEDDING_THEME_CONTENT = "weddingThemeContent"
        const val FOOD_BAR_LIST = "foodBarList"
        const val FOOD_BAR = "foodBar"
        const val AFTER_PARTY_CONTENT = "afterPartyContent"
    }

    private object AdminMappings {
        const val ADMIN = "/admin"
        const val DELETE_ROLES = "/admin/user_roles/delete_roles"
        const val ADD_ROLE = "/admin/user_roles/add_role"
        const val DELETE_SITE_USER = "/admin/site_user/delete_site_user"
        const val ADD_SITE_USER = "/admin/site_user/add_site_user"
        const val ADD_INDEX_CAROUSAL = "/admin/index_carousal/add_index_carousal"
        const val DELETE_INDEX_CAROUSAL = "/admin/index_carousal/delete_carousal"
        const val ADD_EVENT_DATE = "/admin/event_dates/add_event_date"
        const val DELETE_EVENT_DATE = "/admin/event_dates/delete_event_date"
        const val WEDDING_VENUE_CONTENT = "/admin/wedding_venue/edit_venue_text"
        const val WEDDING_VENUE_IMAGE_UPLOAD = "/admin/wedding_venue/venue_image_upload"
        const val WEDDING_VENUE_IMAGE_DELETE = "/admin/wedding_venue/delete_wedding_venue_images"
        const val WEDDING_THEME_CONTENT = "/admin/wedding_theme/content"
        const val WEDDING_THEME_MEN_UPLOAD = "/admin/wedding_theme/men/upload"
        const val WEDDING_THEME_MEN_DELETE = "/admin/wedding_theme/men/delete"
        const val WEDDING_THEME_WOMEN_UPLOAD = "/admin/wedding_theme/women/upload"
        const val WEDDING_THEME_WOMEN_DELETE = "/admin/wedding_theme/women/delete"
        const val FOOD_BAR_ADD = "/admin/food_bar/add"
        const val FOOD_BAR_DELETE = "/admin/food_bar/delete"
        const val EDIT_AFTER_PARTY = "/admin/after_party/edit_after_party"
    }

    private object AdminRequestParams {
        const val WEDDING_VENUE_IMAGES = "venue_images"
        const val IDS = "ids"
        const val MENS_PIC = "men_pictures"
        const val WOMENS_PIC = "women_pictures"
    }

    //Private Extension Functions
    private fun BindingResult.fail(objectName: String, field: String, key: String, properties: Properties) {
        addError(FieldError(objectName, field, properties[key] as String))
    }

    private fun MultipartFile.toPersistedFile(): PersistedFile {
        val persistedFile = PersistedFile(fileName = this.originalFilename,
                mime = this.contentType, bytes = this.bytes, size = this.size)
        persistedFile.hash = persistedFile.hashCode()
        return persistedFile
    }

    private fun Model.addUserRoleList() {
        addAttribute(AdminAttributes.USER_ROLE_LIST, userRoleService.findAll())
    }

    private fun Model.addUserRole(entity: UserRole = UserRole()) {
        addAttribute(AdminAttributes.USER_ROLE, entity)
    }

    private fun Model.addSiteUserList() {
        addAttribute(AdminAttributes.SITE_USER_LIST, siteUserService.findAll())
    }

    private fun Model.addSiteUser(entity: SiteUser = SiteUser()) {
        addAttribute(AdminAttributes.SITE_USER, entity)
    }

    private fun Model.addIndexCarousel(entity: IndexCarousel = IndexCarousel()) {
        addAttribute(AdminAttributes.INDEX_CAROUSEL, entity)
    }

    private fun Model.addIndexCarouselList() {
        addAttribute(AdminAttributes.INDEX_CAROUSEL_LIST, indexCarouselService.findAllEager())
    }

    private fun Model.addEventDate(entity: EventDate) {
        addAttribute(AdminAttributes.EVENT_DATE, entity)
    }

    private fun Model.addEventDateList() {
        addAttribute(AdminAttributes.EVENT_DATE_LIST, eventDateService.findAll())
    }

    private fun Model.addWeddingVenueContent(entity: WeddingVenueContent = weddingVenueContentService.findOrCreate()) {
        addAttribute(AdminAttributes.WEDDING_VENUE_CONTENT, entity)
    }

    private fun Model.addWeddingThemeContent(entity: WeddingThemeContent = weddingThemeContentService.findOrCreate()) {
        addAttribute(AdminAttributes.WEDDING_THEME_CONTENT, entity)
    }

    private fun Model.addFoodBarList(list: List<FoodBarMenu> = foodBarMenuService.findAll()) {
        addAttribute(AdminAttributes.FOOD_BAR_LIST, list)
    }

    private fun Model.addFoodBarContent(entity: FoodBarMenu = FoodBarMenu()) {
        addAttribute(AdminAttributes.FOOD_BAR, entity)
    }

    private fun Model.addAfterPartyContent(entity: AfterPartyInfo = afterPartyContentService.findOrCreate()) {
        addAttribute(AdminAttributes.AFTER_PARTY_CONTENT, entity)
    }

    private fun MessageHandler.showDeletedInfo(name: String, ids: LongArray) {
        showInfo("Deleted $name with ids = ${ids.joinToString()}")
    }

    private fun MessageHandler.showAdded(name: String) {
        showInfo("$name has been added")
    }

    private fun MessageHandler.showUpdated(name: String) {
        showInfo("$name has been updated")
    }

    private fun MessageHandler.showDuplicateError(name: String) {
        showError("$name already exists")
    }

    private fun MessageHandler.showNoSelectionError(name: String) {
        showError("Select some $name first")
    }

    private fun MessageHandler.showError (error : ConstraintViolationException){
        error.constraintViolations.forEach { showError(it.message) }
    }

    private fun Logger.serverError(e: Exception) {
        error(e.toString(), e)
        messageHandler.showError()
    }

    private fun SiteUser.crossFieldValidate(bindingResult: BindingResult, properties: Properties = validationProperties) {
        if (!passwordMatch()) {
            with(bindingResult) {
                fail("SiteUser", "password", "user.password.nomatch", properties)
                fail("SiteUser", "validatePassword", "user.password.nomatch", properties)
            }
        }
        with(siteUserService) {
            if (countByEmail(email) > 0) {
                bindingResult.fail("SiteUser", "email", "user.email.exists", properties)
            }
            if (countByUserName(userName) > 0) {
                bindingResult.fail("SiteUser", "userName", "user.username.exists", properties)
            }
        }
    }

    private fun IndexCarousel.crossFieldValidate(bindingResult: BindingResult, properties: Properties = validationProperties) {
        with(indexCarouselService) {
            if (countByDestinationLink(destinationLink) > 0) {
                bindingResult.fail("IndexCarousel", "destinationLink", "carousal.destination.duplicate", properties)
            }
            if (countByTitle(title) > 0) {
                bindingResult.fail("IndexCarousel", "title", "carousal.title.duplicate", properties)
            }
            if (countByDisplayOrder(displayOrder) > 0) {
                bindingResult.fail("IndexCarousel", "displayOrder", "carousel.order.unique", properties)
            }
        }
        if (uploadedFile?.isEmpty ?: true) {
            bindingResult.fail("carouselEntity", "uploadedFile", "carousel.image.required", validationProperties)
        }
    }

    //Model Attributes used for non-ajax requests
    @ModelAttribute(AdminAttributes.USER_ROLE_LIST)
    fun fetchUserRoleList(): List<UserRole>? = userRoleService.findAll()

    @ModelAttribute(AdminAttributes.USER_ROLE)
    fun fetchUserRole() = UserRole()

    @ModelAttribute(AdminAttributes.SITE_USER_LIST)
    fun fetchSiteUserList(): List<SiteUser>? = siteUserService.findAll()

    @ModelAttribute(AdminAttributes.SITE_USER)
    fun fetchSiteUser() = SiteUser()

    @ModelAttribute(AdminAttributes.INDEX_CAROUSEL)
    fun fetchIndexCarousel() = IndexCarousel()

    @ModelAttribute(AdminAttributes.INDEX_CAROUSEL_LIST)
    fun fetchIndexCarouselList(): List<IndexCarousel>? = indexCarouselService.findAll() //Eagerly load the attached image

    @ModelAttribute(AdminAttributes.EVENT_DATE)
    fun fetchEventDate() = EventDate()

    @ModelAttribute(AdminAttributes.EVENT_DATE_LIST)
    fun fetchEventDateList(): List<EventDate>? = eventDateService.findAll()!!

    @ModelAttribute(AdminAttributes.WEDDING_VENUE_CONTENT)
    fun fetchWeddingVenueContent() = weddingVenueContentService.findOrCreate()

    @ModelAttribute(AdminAttributes.WEDDING_THEME_CONTENT)
    fun fetchWeddingThemeContent() = weddingThemeContentService.findOrCreate()

    @ModelAttribute(AdminAttributes.FOOD_BAR_LIST)
    fun fetchFoodBarList(): List<FoodBarMenu>? = foodBarMenuService.findAll()

    @ModelAttribute(AdminAttributes.FOOD_BAR)
    fun fetchFoodBarContent() = FoodBarMenu()

    @ModelAttribute(AdminAttributes.AFTER_PARTY_CONTENT)
    fun fetchAfterPartyContent() = afterPartyContentService.findOrCreate()

    //Request Mappings
    @GetMapping(AdminMappings.ADMIN)
    fun doGet(): String = AdminOutcomes.ADMIN

    @GetMapping(AdminMappings.DELETE_ROLES)
    fun refreshDeleteRoles(model: Model): String {
        model.addUserRoleList()
        return AdminOutcomes.DELETE_ROLES
    }

    @PostMapping(AdminMappings.DELETE_ROLES)
    fun deleteRoles(@RequestParam(AdminRequestParams.IDS, required = false) ids: LongArray?, model: Model): String {
        if (ids == null) {
            messageHandler.showNoSelectionError("User Roles")
        } else {
            try {
                userRoleService.deleteAll(ids.toList())
                model.addUserRoleList()
                messageHandler.showDeletedInfo("User Roles", ids)
            } catch (e: Exception) {
                logger.serverError(e)
            }
        }
        return AdminOutcomes.DELETE_ROLES
    }

    @PostMapping(AdminMappings.ADD_ROLE)
    fun addRole(@ModelAttribute(AdminAttributes.USER_ROLE) @Valid userRole: UserRole, bindingResult: BindingResult, model: Model): String {
        var entity = userRole
        if (!bindingResult.hasErrors()) {
            try {
                userRoleService.save(userRole)
                entity = UserRole()
                messageHandler.showAdded(userRole.role)
            } catch (e: Exception) {
                when (e) {
                    is DataIntegrityViolationException -> messageHandler.showDuplicateError(userRole.role)
                    is ConstraintViolationException -> messageHandler.showError(e)
                    else -> logger.serverError(e)
                }
            }
        }
        model.addUserRole(entity)
        return AdminOutcomes.ADD_ROLES
    }

    @GetMapping(AdminMappings.DELETE_SITE_USER)
    fun refreshSiteUsers(model: Model): String {
        model.addSiteUserList()
        return AdminOutcomes.DELETE_SITE_USERS
    }

    @PostMapping(AdminMappings.DELETE_SITE_USER)
    fun deleteSelectedSiteUsers(@RequestParam(AdminRequestParams.IDS, required = false) ids: LongArray?, model: Model): String {
        if (ids == null) {
            messageHandler.showNoSelectionError("Users")
        } else {
            try {
                siteUserService.deleteAll(ids.toList())
                model.addSiteUserList()
                messageHandler.showDeletedInfo("Site Users", ids)
            } catch (e: Exception) {
                logger.serverError(e)
            }
        }
        return AdminOutcomes.DELETE_SITE_USERS
    }

    @GetMapping(AdminMappings.ADD_SITE_USER)
    fun refreshSiteUserForm(model: Model): String {
        with(model) {
            addUserRoleList()
            addSiteUser()
        }
        return AdminOutcomes.ADD_SITE_USER
    }

    @PostMapping(AdminMappings.ADD_SITE_USER)
    fun addSiteUser(@Valid siteUser: SiteUser, bindingResult: BindingResult, model: Model): String {
        var entity = siteUser
        siteUser.crossFieldValidate(bindingResult)
        if (!bindingResult.hasErrors()) {
            try {
                siteUser.roles = userRoleService.findAll(siteUser.roleIds).toMutableSet()
                siteUserService.save(siteUser)
                entity = SiteUser()
                messageHandler.showAdded(siteUser.userName)
            } catch (e: Exception) {
                when (e) {
                    is ConstraintViolationException -> messageHandler.showError(e)
                    else -> logger.serverError(e)
                }
            }
        }
        model.addSiteUser(entity)
        return AdminOutcomes.ADD_SITE_USER
    }

    @PostMapping(AdminMappings.ADD_INDEX_CAROUSAL)
    fun addIndexCarousel(@Valid indexCarousel: IndexCarousel,
                         bindingResult: BindingResult, model: Model): String {
        var entity = indexCarousel
        indexCarousel.crossFieldValidate(bindingResult)
        if (!bindingResult.hasErrors()) {
            try {
                indexCarousel.image = indexCarousel.uploadedFile?.toPersistedFile() ?: PersistedFile()
                indexCarouselService.save(indexCarousel)
                messageHandler.showAdded(indexCarousel.title)
                entity = IndexCarousel()
            } catch (e: Exception) {
                when (e) {
                    is DataIntegrityViolationException -> messageHandler.showError("${indexCarousel.image.fileName} is already in use")
                    is ConstraintViolationException -> messageHandler.showError(e)
                    else -> logger.serverError(e)
                }
            }
        }
        model.addIndexCarousel(entity)
        return AdminOutcomes.ADD_INDEX_CAROUSEL
    }

    @GetMapping(AdminMappings.DELETE_INDEX_CAROUSAL)
    fun refreshIndexCarousel(model: Model): String {
        model.addIndexCarouselList()
        return AdminOutcomes.DELETE_CAROUSEL
    }

    @PostMapping(AdminMappings.DELETE_INDEX_CAROUSAL)
    fun deleteIndexCarousel(@RequestParam(AdminRequestParams.IDS, required = false) ids: LongArray?, model: Model): String {
        if(ids == null){
            messageHandler.showNoSelectionError("Index Carousal")
        } else {
            try {
                indexCarouselService.deleteAll(ids.toList())
                model.addIndexCarouselList()
                messageHandler.showDeletedInfo("Index Carousels", ids)
            } catch (e: Exception) {
                when (e) {
                    is ConstraintViolationException -> messageHandler.showError(e)
                    else -> logger.serverError(e)
                }
            }
        }
        return AdminOutcomes.DELETE_CAROUSEL
    }

    @PostMapping(AdminMappings.ADD_EVENT_DATE)
    fun addEventDate(@Valid eventDate: EventDate, bindingResult: BindingResult, model: Model): String {
        var entity = eventDate
        if (!bindingResult.hasErrors()) {
            try {
                eventDateService.save(eventDate)
                entity = EventDate()
                messageHandler.showAdded(eventDate.title)
            } catch (e: Exception) {
                when (e) {
                    is ConstraintViolationException -> messageHandler.showError(e)
                    else -> logger.serverError(e)
                }
            }
        }
        model.addEventDate(entity)
        return AdminOutcomes.ADD_DATE
    }

    @GetMapping(AdminMappings.DELETE_EVENT_DATE)
    fun refreshEventDates(model: Model): String {
        model.addEventDateList()
        return AdminOutcomes.DELETE_EVENT_DATE
    }

    @PostMapping(AdminMappings.DELETE_EVENT_DATE)
    fun deleteEventDate(@RequestParam(AdminRequestParams.IDS, required = false) ids: LongArray?, model: Model): String {
        if(ids == null){
            messageHandler.showNoSelectionError("Event Dates")
        } else {
            try {
                eventDateService.deleteAll(ids.toList())
                model.addEventDateList()
                messageHandler.showDeletedInfo("Event Date", ids)
            } catch (e: Exception) {
                logger.serverError(e)
            }
        }
        return AdminOutcomes.DELETE_EVENT_DATE
    }

    @GetMapping(AdminMappings.WEDDING_VENUE_CONTENT)
    fun fetchWeddingVenueText(model: Model): String {
        model.addWeddingVenueContent()
        return AdminOutcomes.EDIT_WEDDING_VENUE_TEXT
    }

    @PostMapping(AdminMappings.WEDDING_VENUE_CONTENT)
    fun editWeddingVenueText(@ModelAttribute(AdminAttributes.WEDDING_VENUE_CONTENT)
                             @Valid weddingVenueContent: WeddingVenueContent,
                             bindingResult: BindingResult, model: Model): String {
        var entity = weddingVenueContent
        if (!bindingResult.hasErrors()) {
            try {
                entity = weddingVenueContentService.save(weddingVenueContent)
                messageHandler.showUpdated("Wedding Venue Content")
            } catch (e: Exception) {
                when (e) {
                    is ConstraintViolationException -> messageHandler.showError(e)
                    else -> logger.serverError(e)
                }
            }
        }
        model.addWeddingVenueContent(entity)
        return AdminOutcomes.EDIT_WEDDING_VENUE_TEXT
    }

    @PostMapping(AdminMappings.WEDDING_VENUE_IMAGE_UPLOAD)
    fun uploadWeddingVenueImage(
            @RequestParam(AdminRequestParams.WEDDING_VENUE_IMAGES) multipartFile: MultipartFile): String {
        try {
            val weddingVenueEntity = weddingVenueContentService.findOrCreate()
            weddingVenueEntity.images.add(multipartFile.toPersistedFile())
            weddingVenueContentService.save(weddingVenueEntity)
            messageHandler.showAdded(multipartFile.originalFilename)
        } catch (e: Exception) {
            when (e){
                is DataIntegrityViolationException -> messageHandler.showDuplicateError("Venue Image")
                is ConstraintViolationException -> messageHandler.showError(e)
                else -> logger.serverError(e)
            }
        } finally {
            return AdminOutcomes.EDIT_WEDDING_VENUE_IMAGE_UPLOAD
        }
    }

    @GetMapping(AdminMappings.WEDDING_VENUE_IMAGE_DELETE)
    fun refreshDeleteImages(model: Model): String {
        model.addWeddingVenueContent()
        return AdminOutcomes.EDIT_WEDDING_VENUE_IMAGE_DELETE
    }

    @PostMapping(AdminMappings.WEDDING_VENUE_IMAGE_DELETE)
    fun deleteWeddingVenueImage(@RequestParam(AdminRequestParams.IDS, required = false) ids: LongArray?): String {
        if(ids == null){
            messageHandler.showNoSelectionError("Venue Images")
        } else {
            try {
                val weddingVenueContent = weddingVenueContentService.findOrCreate()
                weddingVenueContent.images.removeIf { it.id ?: -1 in ids }
                weddingVenueContentService.save(weddingVenueContent)
                messageHandler.showDeletedInfo("Wedding Venue Images", ids)
            } catch (e: Exception) {
                logger.serverError(e)
            }
        }
        return AdminOutcomes.EDIT_WEDDING_VENUE_IMAGE_DELETE
    }

    @PostMapping(AdminMappings.WEDDING_THEME_CONTENT)
    fun editWeddingThemeContent(@ModelAttribute(AdminAttributes.WEDDING_THEME_CONTENT) @Valid weddingThemeContent: WeddingThemeContent,
                                bindingResult: BindingResult, model: Model): String {
        var entity = weddingThemeContent
        if (!bindingResult.hasErrors()) {
            try {
                entity = weddingThemeContentService.save(weddingThemeContent)
                messageHandler.showUpdated("Wedding Theme Content")
            } catch (e: Exception) {
                when (e) {
                    is ConstraintViolationException -> messageHandler.showError(e)
                    else -> logger.serverError(e)
                }
            }
        }
        model.addWeddingThemeContent(entity)
        return AdminOutcomes.WEDDING_THEME_CONTENT
    }

    @PostMapping(AdminMappings.WEDDING_THEME_MEN_UPLOAD)
    fun uploadMensPictures(@RequestParam(AdminRequestParams.MENS_PIC) multipartFile: MultipartFile): String {
        try {
            val weddingThemeContent = weddingThemeContentService.findOrCreate()
            weddingThemeContent.menExamplePics.add(multipartFile.toPersistedFile())
            weddingThemeContentService.save(weddingThemeContent)
            messageHandler.showAdded(multipartFile.originalFilename)
        } catch (e: Exception) {
            when (e) {
                is ConstraintViolationException -> messageHandler.showError(e)
                else -> logger.serverError(e)
            }
        } finally {
            return AdminOutcomes.WEDDING_THEME_MEN_UPLOAD
        }
    }

    @PostMapping(AdminMappings.WEDDING_THEME_MEN_DELETE)
    fun deleteMensPictures(@RequestParam(AdminRequestParams.IDS, required = false) ids: LongArray?): String {
        if (ids == null){
            messageHandler.showNoSelectionError("Men's pictures")
        } else {
            try {
                val entity = weddingThemeContentService.findOrCreate()
                entity.menExamplePics.removeIf { it.id ?: -1 in ids }
                weddingThemeContentService.save(entity)
                messageHandler.showDeletedInfo("Images", ids)
            } catch (e: Exception) {
                logger.serverError(e)
            }
        }
        return AdminOutcomes.WEDDING_THEME_MEN_DELETE
    }

    @GetMapping(AdminMappings.WEDDING_THEME_MEN_DELETE)
    fun refreshMensPicture(model: Model): String {
        model.addWeddingThemeContent()
        return AdminOutcomes.WEDDING_THEME_MEN_DELETE
    }

    @PostMapping(AdminMappings.WEDDING_THEME_WOMEN_UPLOAD)
    fun uploadWomensPictures(@RequestParam(AdminRequestParams.WOMENS_PIC) multipartFile: MultipartFile): String {
        try {
            val weddingThemeContent = weddingThemeContentService.findOrCreate()
            weddingThemeContent.womenExamplePics.add(multipartFile.toPersistedFile())
            weddingThemeContentService.save(weddingThemeContent)
            messageHandler.showAdded(multipartFile.originalFilename)
        } catch (e: Exception) {
            when (e) {
                is ConstraintViolationException -> messageHandler.showError(e)
                else -> logger.serverError(e)
            }
        } finally {
            return AdminOutcomes.WEDDING_THEME_WOMEN_UPLOAD
        }
    }

    @PostMapping(AdminMappings.WEDDING_THEME_WOMEN_DELETE)
    fun deleteWomensPictures(@RequestParam(AdminRequestParams.IDS, required = false) ids: LongArray?): String {
        if (ids == null){
            messageHandler.showNoSelectionError("Women's pictures")
        } else {
            try {
                val entity = weddingThemeContentService.findOrCreate()
                entity.womenExamplePics.removeIf { it.id ?: -1 in ids }
                weddingThemeContentService.save(entity)
                messageHandler.showDeletedInfo("Images", ids)
            } catch (e: Exception) {
                logger.serverError(e)
            }
        }
        return AdminOutcomes.WEDDING_THEME_WOMEN_DELETE
    }

    @GetMapping(AdminMappings.WEDDING_THEME_WOMEN_DELETE)
    fun refreshWomensPicture(model: Model): String {
        model.addWeddingThemeContent()
        return AdminOutcomes.WEDDING_THEME_WOMEN_DELETE
    }

    @PostMapping(AdminMappings.FOOD_BAR_ADD)
    fun addFoodMenu(@ModelAttribute(AdminAttributes.FOOD_BAR) @Valid foodBarMenu: FoodBarMenu, bindingResult: BindingResult, model: Model): String {
        var entity = foodBarMenu
        if (!bindingResult.hasErrors()) {
            try {
                foodBarMenuService.save(foodBarMenu)
                messageHandler.showAdded(foodBarMenu.title)
                entity = FoodBarMenu()
            } catch (e: Exception) {
                when (e) {
                    is DataIntegrityViolationException -> messageHandler.showDuplicateError("Menu")
                    is ConstraintViolationException -> messageHandler.showError(e)
                    else -> logger.serverError(e)
                }
            }
        }
        model.addFoodBarContent(entity)
        return AdminOutcomes.FOOD_BAR_MENU_ADD
    }

    @PostMapping(AdminMappings.FOOD_BAR_DELETE)
    fun deleteFoodMenu(@RequestParam(AdminRequestParams.IDS, required = false) ids: LongArray?, model: Model): String {
        if (ids == null){
            messageHandler.showNoSelectionError("Food Menu")
        } else {
            try {
                foodBarMenuService.deleteAll(ids)
                model.addFoodBarList()
                messageHandler.showDeletedInfo("Food/Bar Menus", ids)
            } catch (e: Exception) {
                logger.serverError(e)
            }
        }
        return AdminOutcomes.FOOD_BAR_MENU_DELETE
    }

    @GetMapping(AdminMappings.FOOD_BAR_DELETE)
    fun refreshFoodMenu(model: Model): String {
        model.addFoodBarList()
        return AdminOutcomes.FOOD_BAR_MENU_DELETE
    }

    @PostMapping(AdminMappings.EDIT_AFTER_PARTY)
    fun updateAfterPartyContent(@ModelAttribute(AdminAttributes.AFTER_PARTY_CONTENT) @Valid afterPartyInfo: AfterPartyInfo,
                                bindingResult: BindingResult, model: Model): String {
        var entity = afterPartyInfo
        if (!bindingResult.hasErrors()) {
            try {
                entity = afterPartyContentService.save(afterPartyInfo)
                messageHandler.showUpdated("After Party Content")
            } catch (e: Exception) {
                when (e) {
                    is ConstraintViolationException -> messageHandler.showError(e)
                    else -> logger.serverError(e)
                }
            }
        }
        model.addAfterPartyContent(entity)
        return AdminOutcomes.EDIT_AFTER_PARTY
    }
}

@Controller
class IndexController(
        @Autowired
        private val indexCarouselService: IndexCarouselService) {

    private object IndexMappings {
        const val INDEX = "/"
    }

    private object IndexOutcomes {
        const val INDEX = "index"
    }

    private object IndexAttributes {
        const val INDEX_CAROUSELS = "indexCarousels"
    }

    fun Model.addIndexCarousels(indexCarousel: List<IndexCarousel> = indexCarouselService.findAll()) {
        addAttribute(IndexAttributes.INDEX_CAROUSELS, indexCarousel)
    }

    @GetMapping(IndexMappings.INDEX)
    fun doGet(model: Model): String {
        model.addIndexCarousels()
        return IndexOutcomes.INDEX
    }
}

@Controller
class WeddingReceptionController(
        @Autowired private val weddingVenueContentService: WeddingVenueContentService,
        @Autowired private val weddingThemeContentService: WeddingThemeContentService,
        @Autowired private val foodBarMenuService: FoodBarMenuService,
        @Autowired private val afterPartyContentService: AfterPartyContentService) {

    private object WeddingReceptionMappings {
        const val WEDDING_RECEPTION = "/wedding_reception"
    }

    private object WeddingReceptionOutcomes {
        const val WEDDING_RECEPTION_OUTCOME = "/wedding_reception"
    }

    private object WeddingReceptionAttributes {
        const val WEDDING_VENUE_CONTENT = "weddingVenueContentEntity"
        const val WEDDING_THEME_CONTENT = "weddingThemeContent"
        const val FOOD_BAR_CONTENT = "foodBarContent"
        const val AFTER_PARTY_CONTENT = "afterPartyContent"
    }

    @ModelAttribute(WeddingReceptionAttributes.WEDDING_VENUE_CONTENT)
    fun fetchWeddingReceptionContent() = weddingVenueContentService.findOrCreate()

    @ModelAttribute(WeddingReceptionAttributes.WEDDING_THEME_CONTENT)
    fun fetchWeddingThemeContent() = weddingThemeContentService.findOrCreate()

    @ModelAttribute(WeddingReceptionAttributes.FOOD_BAR_CONTENT)
    fun fetchFoodBarMenuContent(): List<FoodBarMenu>? = foodBarMenuService.findAll()

    @ModelAttribute(WeddingReceptionAttributes.AFTER_PARTY_CONTENT)
    fun fetchAfterPartyContent() = afterPartyContentService.findOrCreate()

    @GetMapping(WeddingReceptionMappings.WEDDING_RECEPTION)
    fun doGet(): String {
        return WeddingReceptionOutcomes.WEDDING_RECEPTION_OUTCOME
    }
}
