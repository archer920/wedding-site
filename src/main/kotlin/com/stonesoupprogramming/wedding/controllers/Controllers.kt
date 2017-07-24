package com.stonesoupprogramming.wedding.controllers

import com.stonesoupprogramming.wedding.entities.*
import com.stonesoupprogramming.wedding.services.*
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Scope
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Component
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
import javax.validation.Valid

interface UiMessageHandler {

    @ModelAttribute("errorMessages")
    fun errorMessages() : List<String>

    @ModelAttribute("infoMessages")
    fun infoMessages() : List<String>

    @GetMapping("/messages")
    fun fetchMessages(model: Model): String

    fun showError()
    fun showError(error: String)
    fun showError(errorList: List<String>)

    fun showInfo(info : String)
    fun showInfo(infoList : List<String>)

    fun populateModel(model: Model)
}

@Component
@Scope("prototype")
@Primary
class UiMessageHandlerImpl(
        @Autowired @Qualifier("ValidationProperties")
        private var validationProperties : Properties) : UiMessageHandler {

    private val OUTCOME = "/fragments/master/alerts :: alerts"

    private val errorMessages: MutableSet<String> = mutableSetOf()
    private val infoMessages: MutableSet<String> = mutableSetOf()

    override fun showError(){
        errorMessages.add(validationProperties.getProperty("general.server.error") ?: "")
    }

    override fun showError(error: String) {
        errorMessages.add(error)
    }

    override fun showError(errorList: List<String>) {
        errorMessages.addAll(errorList)
    }

    override fun showInfo(info: String) {
        infoMessages.add(info)
    }

    override fun showInfo(infoList: List<String>) {
        infoMessages.addAll(infoList)
    }

    override fun errorMessages(): List<String> = errorMessages.toList()

    override fun infoMessages(): List<String> = infoMessages.toList()

    override fun populateModel(model: Model) {
        model.apply {
            addAttribute("errorMessages", errorMessages.toList())
            addAttribute("infoMessages", infoMessages.toList())
        }
        errorMessages.clear()
        infoMessages.clear()
    }

    override fun fetchMessages(model: Model): String {
        populateModel(model)
        return OUTCOME
    }
}

interface BannerController {

    @GetMapping("/navbarLinks")
    fun refreshNavBarLinks(model: Model) : String

    @ModelAttribute("navbarLinks")
    fun navBarLinks(): List<CarouselEntity>

    @ModelAttribute("daysRemaining")
    fun fetchDaysRemaining(): Long
}

@Controller
class BannerControllerImpl(
        @Autowired private val carouselService: CarouselService,
        @Autowired private val eventDateService: EventDateService) : BannerController {

    override fun fetchDaysRemaining() : Long =
            eventDateService.getByDateType(DateType.Wedding).calcRemainingDays()


    private val OUTCOME = "fragments/master/banner :: banner"

    override fun refreshNavBarLinks(model: Model): String {
        model.apply {
            addAttribute("navbarLinks", carouselService.findAll())
            addAttribute("daysRemaining", eventDateService.getByDateType(DateType.Wedding).calcRemainingDays())
        }
        return OUTCOME
    }

    override fun navBarLinks(): List<CarouselEntity> = carouselService.findAll()
}



@Controller
class AdminController(@Autowired private val logger: Logger,
                      @Autowired @Qualifier("ValidationProperties") private val validationProperties: Properties,
                      @Autowired private val uiMessageHandler: UiMessageHandler,
                      @Autowired private val siteUserService: SiteUserService,
                      @Autowired private val roleService: RoleService,
                      @Autowired private val carouselService: CarouselService,
                      @Autowired private val eventDateService: EventDateService,
                      @Autowired private val weddingVenueContentService: WeddingVenueContentService,
                      @Autowired private val weddingThemeContentService: WeddingThemeContentService) :
        UiMessageHandler by uiMessageHandler {

    //Compile Time Constants Used in Controller
    private object AdminOutcomes {
        const val ADMIN = "admin"
        const val DELETE_ROLES = "fragments/admin/delete_roles_form :: delete_roles"
        const val ADD_ROLES = "fragments/admin/add_roles_form :: add_roles"
        const val DELETE_SITE_USERS = "fragments/admin/delete_site_users :: delete_site_users"
        const val ADD_SITE_USER = "fragments/admin/add_site_user :: add_site_user"
        const val ADD_INDEX_CAROUSEL = "fragments/admin/add_index_carousel :: add_index_carousel"
        const val DELETE_CAROUSEL = "fragments/admin/delete_carousel_form :: delete_carousel"
        const val ADD_DATE = "fragments/admin/add_date_form :: add_date"
        const val DELETE_EVENT_DATE = "fragments/admin/delete_date_form :: delete_event_date"
        const val EDIT_WEDDING_VENUE_TEXT = "/fragments/admin/edit_wedding_venue_text :: edit_venue_text"
        const val EDIT_WEDDING_VENUE_IMAGE_UPLOAD = "/fragments/admin/edit_wedding_venue_image_upload :: venue_image_upload"
        const val EDIT_WEDDING_VENUE_IMAGE_DELETE = "/fragments/admin/edit_wedding_venue_image_delete :: delete_wedding_venue_images"
        const val WEDDING_THEME_CONTENT = "/fragments/admin/wedding_theme/wedding_theme_content :: wedding_theme_content"
        const val WEDDING_THEME_MEN_UPLOAD = "/fragments/admin/wedding_theme/men_picture_upload :: men_picture_upload"
        const val WEDDING_THEME_MEN_DELETE = "/fragments/admin/wedding_theme/delete_mens_pictures :: men_picture_delete"
        const val WEDDING_THEME_WOMEN_UPLOAD = "/fragments/admin/wedding_theme/women_picture_upload :: women_picture_upload"
        const val WEDDING_THEME_WOMEN_DELETE = "/fragments/admin/wedding_theme/delete_womens_pictures :: women_picture_delete"
    }

    private object AdminAttributes {
        const val ROLE_LIST = "roleList"
        const val ROLE_ENTITY = "roleEntity"
        const val SITE_USER_LIST = "siteUserList"
        const val SITE_USER_ENTITY = "siteUserEntity"
        const val CAROUSEL_LIST = "carouselList"
        const val CAROUSEL_ENTITY = "carouselEntity"
        const val DATE_ENTITY = "dateEntity"
        const val DATE_LIST = "eventDateList"
        const val WEDDING_VENUE_CONTENT_ENTITY = "weddingVenueContentEntity"
        const val WEDDING_THEME_CONTENT = "weddingThemeContent"
    }

    private object AdminMappings {
        const val ADMIN = "/admin"
        const val DELETE_ROLES = "/admin/delete_roles"
        const val ADD_ROLE = "/add_role"
        const val DELETE_SITE_USER = "/admin/delete_site_user"
        const val ADD_SITE_USER = "/admin/add_site_user"
        const val ADD_INDEX_CAROUSEL = "/add_index_carousl"
        const val DELETE_INDEX_CAROUSEL = "/admin/delete_carousel"
        const val ADD_EVENT_DATE = "/admin/add_event_date"
        const val DELETE_EVENT_DATE = "/admin/delete_event_date"
        const val WEDDING_VENUE_CONTENT = "/admin/edit_venue_text"
        const val WEDDING_VENUE_IMAGE_UPLOAD = "/admin/venue_image_upload"
        const val WEDDING_VENUE_IMAGE_DELETE = "/admin/delete_wedding_venue_images"
        const val WEDDING_THEME_CONTENT = "/admin/wedding_theme/content"
        const val WEDDING_THEME_MEN_UPLOAD = "/admin/wedding_theme/men/upload"
        const val WEDDING_THEME_MEN_DELETE = "/admin/wedding_theme/men/delete"
        const val WEDDING_THEME_WOMEN_UPLOAD = "/admin/wedding_theme/women/upload"
        const val WEDDING_THEME_WOMEN_DELETE = "/admin/wedding_theme/women/delete"
    }

    private object AdminRequestParams {
        const val WEDDING_VENUE_IMAGES = "venue_images"
        const val IDS = "ids"
        const val MENS_PIC = "men_pictures"
        const val WOMENS_PIC = "women_pictures"
    }

    //Private Extension Functions
    private fun BindingResult.fail(objectName : String, field: String, key: String, properties: Properties){
        addError(FieldError(objectName, field, properties[key] as String))
    }

    private fun MultipartFile.toPersistedFileEnity(): PersistedFileEntity {
        val persistedFile = PersistedFileEntity(fileName = this.originalFilename,
                mime = this.contentType, bytes = this.bytes, size = this.size)
        persistedFile.hash = persistedFile.hashCode()
        return persistedFile
    }

    private fun Model.addRoleList(){
        addAttribute(AdminAttributes.ROLE_LIST, roleService.findAll())
    }

    private fun Model.addRoleEntity(entity: RoleEntity = RoleEntity()){
        addAttribute(AdminAttributes.ROLE_ENTITY, entity)
    }

    private fun Model.addSiteEntitylist(){
        addAttribute(AdminAttributes.SITE_USER_LIST, siteUserService.findAll())
    }

    private fun Model.addSiteUserEntity(entity : SiteUserEntity = SiteUserEntity()){
        addAttribute(AdminAttributes.SITE_USER_ENTITY, entity)
    }

    private fun Model.addCarouselEntity(entity: CarouselEntity = CarouselEntity()){
        addAttribute(AdminAttributes.CAROUSEL_ENTITY, entity)
    }

    private fun Model.addCarouselList(){
        addAttribute(AdminAttributes.CAROUSEL_LIST, carouselService.findAllEager())
    }

    private fun Model.addEventDateEntity(entity: EventDateEntity){
        addAttribute(AdminAttributes.DATE_ENTITY, entity)
    }

    private fun Model.addEventDateList(){
        addAttribute(AdminAttributes.DATE_LIST, eventDateService.findAll())
    }

    private fun Model.addWeddingVenueContent(entity: WeddingVenueContent = weddingVenueContentService.findOrCreate()){
        addAttribute(AdminAttributes.WEDDING_VENUE_CONTENT_ENTITY, entity)
    }

    private fun Model.addWeddingThemeContent(entity: WeddingThemeContent = weddingThemeContentService.findOrCreate()){
        addAttribute(AdminAttributes.WEDDING_THEME_CONTENT, entity)
    }

    //Model Attributes used for non-ajax requests
    @ModelAttribute(AdminAttributes.ROLE_LIST)
    fun fetchRoleList() = roleService.findAll()!!

    @ModelAttribute(AdminAttributes.ROLE_ENTITY)
    fun fetchRoleEntity() = RoleEntity()

    @ModelAttribute(AdminAttributes.SITE_USER_LIST)
    fun fetchSiteUserList() = siteUserService.findAll()!!

    @ModelAttribute(AdminAttributes.SITE_USER_ENTITY)
    fun fetchSiteUserEntity() = SiteUserEntity()

    @ModelAttribute(AdminAttributes.CAROUSEL_ENTITY)
    fun fetchCarouselEntity() = CarouselEntity()

    @ModelAttribute(AdminAttributes.CAROUSEL_LIST)
    fun fetchCarouselList() = carouselService.findAllEager() //Eagerly load the attached image

    @ModelAttribute(AdminAttributes.DATE_ENTITY)
    fun fetchDateEntity() = EventDateEntity()

    @ModelAttribute(AdminAttributes.DATE_LIST)
    fun fetchEventDateList() = eventDateService.findAll()!!

    @ModelAttribute(AdminAttributes.WEDDING_VENUE_CONTENT_ENTITY)
    fun fetchWeddingContentEntity() = weddingVenueContentService.findOrCreate()

    @ModelAttribute(AdminAttributes.WEDDING_THEME_CONTENT)
    fun fetchWeddingThemeContent() = weddingThemeContentService.findOrCreate()

    @GetMapping(AdminMappings.ADMIN)
    fun doGet(): String = AdminOutcomes.ADMIN

    @GetMapping(AdminMappings.DELETE_ROLES)
    fun refreshDeleteRoles(model: Model): String {
        model.addRoleList()
        return AdminOutcomes.DELETE_ROLES
    }

    @PostMapping(AdminMappings.DELETE_ROLES)
    fun deleteRoles(@RequestParam(name = "ids") ids: LongArray, model: Model): String {
        try {
            roleService.deleteAll(ids.toList())
            model.addRoleList()

            showInfo("Deleted roles with ids = ${ids.joinToString()}")
        } catch (e: Exception) {
            logger.error(e.toString(), e)
            showError()
        } finally {
            return AdminOutcomes.DELETE_ROLES
        }
    }

    @PostMapping(AdminMappings.ADD_ROLE)
    fun addRole(@ModelAttribute @Valid roleEntity: RoleEntity, bindingResult: BindingResult, model: Model): String {
        var entity = roleEntity

        if (!bindingResult.hasErrors()) {
            if (roleService.countByRole(roleEntity.role) == 0L) {
                try {
                    roleService.save(roleEntity)
                    entity = RoleEntity()

                    showInfo("Role ${roleEntity.role} has been added")
                } catch (e: Exception) {
                    logger.error(e.toString(), e)
                    showError()
                }
            } else {
                bindingResult.fail("roleEntity", "role", "role.name.duplicate", validationProperties)
            }
        }
        model.addRoleEntity(entity)
        return AdminOutcomes.ADD_ROLES
    }

    @GetMapping(AdminMappings.DELETE_SITE_USER)
    fun refreshSiteUsers(model: Model): String {
        model.addSiteEntitylist()
        return AdminOutcomes.DELETE_SITE_USERS
    }

    @PostMapping(AdminMappings.DELETE_SITE_USER)
    fun deleteSelectedSiteUsers(@RequestParam(name = "ids") ids: LongArray, model: Model): String {
        try {
            siteUserService.deleteAll(ids.toList())
            model.addSiteEntitylist()

            showInfo("Deleted users with ids = ${ids.joinToString()}")
        } catch (e: Exception) {
            logger.error(e.toString(), e)

            showError()
        } finally {
            return AdminOutcomes.DELETE_SITE_USERS
        }
    }

    @GetMapping(AdminMappings.ADD_SITE_USER)
    fun refreshSiteUserForm(model: Model): String {
        model.apply {
            addRoleList()
            addSiteUserEntity()
        }
        return AdminOutcomes.ADD_SITE_USER
    }

    @PostMapping(AdminMappings.ADD_SITE_USER)
    fun addSiteUser(@ModelAttribute @Valid siteUserEntity: SiteUserEntity, bindingResult: BindingResult, model: Model): String {
        var entity = siteUserEntity

        if (!bindingResult.hasErrors()) {
            if (validate(
                    failCondition = { siteUserService.siteUserRepository.countByUserName(siteUserEntity.userName) > 0L },
                    bindingResult = bindingResult,
                    objectName = "siteUserEntity",
                    field = "userName",
                    message = "user.username.exists"
            ) && validate(
                    failCondition = { siteUserService.siteUserRepository.countByEmail(siteUserEntity.email) > 0L },
                    bindingResult = bindingResult,
                    objectName = "siteUserEntity",
                    field = "email",
                    message = "user.email.exists"
            ) && validate(
                    failCondition = { !siteUserEntity.passwordMatch() },
                    bindingResult = bindingResult,
                    objectName = "siteUserEntity",
                    field = "validatePassword",
                    message = "user.passwords.nomatch"
            )) {
                val roles = roleService.findAll(siteUserEntity.roleIds.toMutableList()).toMutableSet()
                siteUserEntity.roles = roles

                try {
                    siteUserService.save(siteUserEntity)
                    showInfo("Added user ${siteUserEntity.userName}")
                    entity = SiteUserEntity()
                } catch (e: Exception) {
                    logger.error(e.toString(), e)
                    showError()
                }
            }
        }
        model.addSiteUserEntity(entity)
        return AdminOutcomes.ADD_SITE_USER
    }

    @PostMapping(AdminMappings.ADD_INDEX_CAROUSEL)
    fun addIndexCarousel(@Valid carouselEntity: CarouselEntity,
                         bindingResult: BindingResult, model: Model): String {
        var entity = carouselEntity
        if (!bindingResult.hasErrors()) {
            if (carouselEntity.uploadedFile?.isEmpty ?: true) {
                bindingResult.fail("carouselEntity", "uploadedFile", "carousel.image.required", validationProperties)
            } else if (carouselService.countByDisplayOrder(carouselEntity.displayOrder) > 0){
                bindingResult.fail("carouselEntity", "displayOrder", "carousel.order.unique", validationProperties)
            } else {
                try {
                    carouselEntity.image = carouselEntity.uploadedFile?.toPersistedFileEnity() ?: PersistedFileEntity()
                    carouselService.save(carouselEntity)

                    showInfo("Carousel Saved Successfully")
                    entity = CarouselEntity()
                } catch (e: Exception) {
                    when (e) {
                        is DataIntegrityViolationException -> showError("Image already exists in the database")
                        else -> showError()
                    }
                    logger.error(e.toString(), e)
                }
            }
        }
        model.addCarouselEntity(entity)
        return AdminOutcomes.ADD_INDEX_CAROUSEL
    }

    @GetMapping(AdminMappings.DELETE_INDEX_CAROUSEL)
    fun refreshIndexCarousel(model: Model): String {
        model.addCarouselList()
        return AdminOutcomes.DELETE_CAROUSEL
    }

    @PostMapping(AdminMappings.DELETE_INDEX_CAROUSEL)
    fun deleteIndexCarousel(@RequestParam("ids") ids: LongArray, model: Model): String {
        try {
            carouselService.deleteAll(ids.toList())
            model.addCarouselList()

            showInfo("Deleted Carousels with ids ${ids.joinToString()}")
        } catch (e: Exception) {
            logger.error(e.toString(), e)
            showError()
        } finally {
            return AdminOutcomes.DELETE_CAROUSEL
        }
    }

    @PostMapping(AdminMappings.ADD_EVENT_DATE)
    fun addEventDate(@Valid eventDateEntity: EventDateEntity, bindingResult: BindingResult, model: Model) : String {
        var entity = eventDateEntity
        if(!bindingResult.hasErrors()){
            try {
                eventDateService.save(entity)
                entity = EventDateEntity()

                showInfo("Added date")
            } catch (e : Exception){
                logger.error(e.toString(), e)
                showError()
            }
        }
        model.addEventDateEntity(entity)
        return AdminOutcomes.ADD_DATE
    }

    @GetMapping(AdminMappings.DELETE_EVENT_DATE)
    fun refreshEventDates(model : Model) : String {
        model.addEventDateList()
        return AdminOutcomes.DELETE_EVENT_DATE
    }

    @PostMapping(AdminMappings.DELETE_EVENT_DATE)
    fun deleteEventDate(@RequestParam("ids") ids: LongArray, model: Model) : String {
        try{
            eventDateService.deleteAll(ids.toList())
            model.addEventDateList()

            showInfo("Deleted Event Dates with ids ${ids.joinToString()}")
        } catch (e : Exception){
            logger.error(e.toString(), e)
            showError()
        } finally {
            return AdminOutcomes.DELETE_EVENT_DATE
        }
    }

    @GetMapping(AdminMappings.WEDDING_VENUE_CONTENT)
    fun fetchWeddingVenueText(model : Model) : String {
        model.addWeddingVenueContent()
        return AdminOutcomes.EDIT_WEDDING_VENUE_TEXT
    }

    @PostMapping(AdminMappings.WEDDING_VENUE_CONTENT)
    fun editWeddingVenueText(@ModelAttribute(AdminAttributes.WEDDING_VENUE_CONTENT_ENTITY) @Valid weddingVenueContent: WeddingVenueContent, bindingResult: BindingResult, model: Model) : String {
        var entity = weddingVenueContent
        if(!bindingResult.hasErrors()){
            try{
                entity = weddingVenueContentService.save(weddingVenueContent)
                showInfo("Updated wedding text")
            } catch (e : Exception){
                logger.error(e.toString(), e)
                showError()
            }
        }
        model.addWeddingVenueContent(entity)
        return AdminOutcomes.EDIT_WEDDING_VENUE_TEXT
    }

    @PostMapping(AdminMappings.WEDDING_VENUE_IMAGE_UPLOAD)
    fun uploadWeddingVenueImage(@RequestParam(AdminRequestParams.WEDDING_VENUE_IMAGES) multipartFile: MultipartFile) : String {
        try{
            val weddingVenueEntity = weddingVenueContentService.findOrCreate()
            weddingVenueEntity.images.add(multipartFile.toPersistedFileEnity())
            weddingVenueContentService.save(weddingVenueEntity)
            showInfo("${multipartFile.originalFilename} has been saved")
        } catch (e : Exception){
            showError()
        } finally {
            return AdminOutcomes.EDIT_WEDDING_VENUE_IMAGE_UPLOAD
        }
    }

    @GetMapping(AdminMappings.WEDDING_VENUE_IMAGE_DELETE)
    fun refreshDeleteImages(model : Model) : String {
        model.addWeddingVenueContent()
        return AdminOutcomes.EDIT_WEDDING_VENUE_IMAGE_DELETE
    }

    @PostMapping(AdminMappings.WEDDING_VENUE_IMAGE_DELETE)
    fun deleteWeddingVenueImage(@RequestParam(AdminRequestParams.IDS) ids : LongArray) : String {
        try {
            val weddingVenueContent = weddingVenueContentService.findOrCreate()
            weddingVenueContent.images.removeIf { it.id?: -1  in ids }
            weddingVenueContentService.save(weddingVenueContent)
            showInfo("Removed images with ids ${ids.joinToString()}")
        } catch (e : Exception){
            logger.error(e.toString(), e)
            showError()
        } finally {
            return AdminOutcomes.EDIT_WEDDING_VENUE_IMAGE_DELETE
        }
    }

    @PostMapping(AdminMappings.WEDDING_THEME_CONTENT)
    fun editWeddingThemeContent(@ModelAttribute(AdminAttributes.WEDDING_THEME_CONTENT) weddingThemeContent: WeddingThemeContent,
                                bindingResult: BindingResult, model: Model) : String {
        var entity = weddingThemeContent
        if(!bindingResult.hasErrors()){
            try{
                entity = weddingThemeContentService.save(weddingThemeContent)
                showInfo("Wedding Theme Content Updated")
            } catch (e : Exception){
                logger.error(e.toString(), e)
                showError()
            }
        }
        model.addWeddingThemeContent(entity)
        return AdminOutcomes.WEDDING_THEME_CONTENT
    }

    @PostMapping(AdminMappings.WEDDING_THEME_MEN_UPLOAD)
    fun uploadMensPictures(@RequestParam(AdminRequestParams.MENS_PIC) multipartFile: MultipartFile) : String {
        try {
            val weddingThemeContent = weddingThemeContentService.findOrCreate()
            weddingThemeContent.menExamplePics.add(multipartFile.toPersistedFileEnity())
            weddingThemeContentService.save(weddingThemeContent)
            showInfo("${multipartFile.originalFilename} has been saved")
        } catch (e : Exception){
            logger.error(e.toString(), e)
            showError()
        } finally {
            return AdminOutcomes.WEDDING_THEME_MEN_UPLOAD
        }
    }

    @PostMapping(AdminMappings.WEDDING_THEME_MEN_DELETE)
    fun deleteMensPictures(@RequestParam(AdminRequestParams.IDS) ids: LongArray) : String {
        try {
            val entity = weddingThemeContentService.findOrCreate()
            entity.menExamplePics.removeIf { it.id ?: -1 in ids }
            weddingThemeContentService.save(entity)
            showInfo("Removed images with ids ${ids.joinToString()}")
        } catch (e : Exception){
            logger.error(e.toString(), e)
            showError()
        } finally {
            return AdminOutcomes.WEDDING_THEME_MEN_DELETE
        }
    }

    @GetMapping(AdminMappings.WEDDING_THEME_MEN_DELETE)
    fun refreshMensPicture(model: Model) : String {
        model.addWeddingThemeContent()
        return AdminOutcomes.WEDDING_THEME_MEN_DELETE
    }

    @PostMapping(AdminMappings.WEDDING_THEME_WOMEN_UPLOAD)
    fun uploadWomensPictures(@RequestParam(AdminRequestParams.WOMENS_PIC) multipartFile: MultipartFile) : String {
        try {
            val weddingThemeContent = weddingThemeContentService.findOrCreate()
            weddingThemeContent.womenExamplePics.add(multipartFile.toPersistedFileEnity())
            weddingThemeContentService.save(weddingThemeContent)
            showInfo("${multipartFile.originalFilename} has been saved")
        } catch (e : Exception){
            logger.error(e.toString(), e)
            showError()
        } finally {
            return AdminOutcomes.WEDDING_THEME_MEN_UPLOAD
        }
    }

    @PostMapping(AdminMappings.WEDDING_THEME_WOMEN_DELETE)
    fun deleteWomensPictures(@RequestParam(AdminRequestParams.IDS) ids: LongArray) : String {
        try {
            val entity = weddingThemeContentService.findOrCreate()
            entity.womenExamplePics.removeIf { it.id ?: -1 in ids }
            weddingThemeContentService.save(entity)
            showInfo("Removed images with ids ${ids.joinToString()}")
        } catch (e : Exception){
            logger.error(e.toString(), e)
            showError()
        } finally {
            return AdminOutcomes.WEDDING_THEME_WOMEN_DELETE
        }
    }

    @GetMapping(AdminMappings.WEDDING_THEME_WOMEN_DELETE)
    fun refreshWomensPicture(model: Model) : String {
        model.addWeddingThemeContent()
        return AdminOutcomes.WEDDING_THEME_WOMEN_DELETE
    }

    private fun validate(failCondition: () -> Boolean,
                         bindingResult: BindingResult,
                         objectName: String,
                         field: String,
                         message: String): Boolean {
        var pass = true
        if (failCondition.invoke()) {
            bindingResult.addError(FieldError(objectName, field, validationProperties[message] as String))
            pass = false
        }
        return pass
    }
}

@Controller
class IndexController(
        @Autowired
        private val carouselService: CarouselService){

    @GetMapping("/")
    fun doGet(model : Model) : String {
        model.addAttribute("carousels",
                carouselService.findAllEager())
        return "index"
    }
}

@Controller
class WeddingReceptionController(
        @Autowired private val weddingVenueContentService: WeddingVenueContentService,
        @Autowired private val weddingThemeContentService: WeddingThemeContentService){

    private object WeddingReceptionMappings{
        const val WEDDING_RECEPTION = "/wedding_reception"
    }

    private object WeddingReceptionOutcomes {
        const val WEDDING_RECEPTION_OUTCOME = "/wedding_reception"
    }

    private object WeddingReceptionAttributes {
        const val WEDDING_VENUE_CONTENT = "weddingVenueContentEntity"
        const val WEDDING_THEME_CONTENT = "weddingThemeContent"
    }

    @ModelAttribute(WeddingReceptionAttributes.WEDDING_VENUE_CONTENT)
    fun fetchWeddingReceptionContent() = weddingVenueContentService.findOrCreate()

    @ModelAttribute(WeddingReceptionAttributes.WEDDING_THEME_CONTENT)
    fun fetchWeddingThemeContent() = weddingThemeContentService.findOrCreate()

    @GetMapping(WeddingReceptionMappings.WEDDING_RECEPTION)
    fun doGet() : String{
        return WeddingReceptionOutcomes.WEDDING_RECEPTION_OUTCOME
    }
}
