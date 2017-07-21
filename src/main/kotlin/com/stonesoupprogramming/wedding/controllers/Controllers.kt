package com.stonesoupprogramming.wedding.controllers

import com.stonesoupprogramming.wedding.entities.*
import com.stonesoupprogramming.wedding.extensions.fail
import com.stonesoupprogramming.wedding.extensions.toPersistedFileEnity
import com.stonesoupprogramming.wedding.services.CarouselService
import com.stonesoupprogramming.wedding.services.EventDateService
import com.stonesoupprogramming.wedding.services.RoleService
import com.stonesoupprogramming.wedding.services.SiteUserService
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
                      @Autowired private val eventDateService: EventDateService) :
        UiMessageHandler by uiMessageHandler {

    private val ADMIN = "admin"
    private val DELETE_ROLES = "fragments/admin/delete_roles_form :: delete_roles"
    private val ADD_ROLES = "fragments/admin/add_roles_form :: add_roles"
    private val DELETE_SITE_USERS = "fragments/admin/delete_site_users :: delete_site_users"
    private val ADD_SITE_USER = "fragments/admin/add_site_user :: add_site_user"
    private val ADD_INDEX_CAROUSEL = "fragments/admin/add_index_carousel :: add_index_carousel"
    private val DELETE_CAROUSEL = "fragments/admin/delete_carousel_form :: delete_carousel"
    private val ADD_DATE = "fragments/admin/add_date_form :: add_date"
    private val DELETE_EVENT_DATE = "fragments/admin/delete_date_form :: delete_event_date"

    //Model Attributes used for non-ajax requests
    @ModelAttribute("roleList")
    fun fetchRoleList() = roleService.findAll()

    @ModelAttribute("roleEntity")
    fun fetchRoleEntity() = RoleEntity()

    @ModelAttribute("siteUserList")
    fun fetchSiteUserList() = siteUserService.findAll()

    @ModelAttribute("siteUserEntity")
    fun fetchSiteUserEntity() = SiteUserEntity()

    @ModelAttribute("carouselEntity")
    fun fetchCarouselEntity() = CarouselEntity()

    @ModelAttribute("carouselList")
    fun fetchCarouselList() = carouselService.findAllEager() //Eagerly load the attached image

    @ModelAttribute("dateEntity")
    fun fetchDateEntity() = EventDateEntity()

    @ModelAttribute("eventDateList")
    fun fetchEventDateList() = eventDateService.findAll()

    @GetMapping("/admin")
    fun doGet(): String = ADMIN

    @GetMapping("/admin/delete_roles")
    fun refreshDeleteRoles(model: Model): String {
        model.addAttribute("roleList", roleService.findAll())
        return DELETE_ROLES
    }

    @PostMapping("/admin/delete_roles")
    fun deleteRoles(@RequestParam(name = "ids") ids: LongArray, model: Model): String {
        try {
            roleService.deleteAll(ids.toList())
            model.addAttribute("roleList", roleService.findAll())

            showInfo("Deleted roles with ids = ${ids.joinToString()}")
        } catch (e: Exception) {
            logger.error(e.toString(), e)
            showError()
        } finally {
            return DELETE_ROLES
        }
    }

    @PostMapping("/admin/add_role")
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
        model.addAttribute("roleEntity", entity)
        return ADD_ROLES
    }

    @GetMapping("/admin/delete_site_user")
    fun refreshSiteUsers(model: Model): String {
        model.addAttribute("siteUserList", siteUserService.findAll())
        return DELETE_SITE_USERS
    }

    @PostMapping("/admin/delete_site_user")
    fun deleteSelectedSiteUsers(@RequestParam(name = "ids") ids: LongArray, model: Model): String {
        try {
            siteUserService.deleteAll(ids.toList())
            model.addAttribute("siteUserList", siteUserService.findAll())

            showInfo("Deleted users with ids = ${ids.joinToString()}")
        } catch (e: Exception) {
            logger.error(e.toString(), e)

            showError()
        } finally {
            return DELETE_SITE_USERS
        }
    }

    @GetMapping("/admin/add_site_user")
    fun refreshSiteUserForm(model: Model): String {
        model.apply {
            addAttribute("roleList", roleService.findAll())
            addAttribute("siteUserEntity", SiteUserEntity())
        }
        return ADD_SITE_USER
    }

    @PostMapping("/admin/add_site_user")
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
        model.addAttribute("siteUserEntity", entity)
        return ADD_SITE_USER
    }

    @PostMapping("/admin/add_index_carousel")
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
        model.addAttribute("carouselEntity", entity)
        return ADD_INDEX_CAROUSEL
    }

    @GetMapping("/admin/delete_carousel")
    fun refreshIndexCarousel(model: Model): String {
        model.addAttribute("carouselList", carouselService.findAllEager())
        return DELETE_CAROUSEL
    }

    @PostMapping("/admin/delete_carousel")
    fun deleteIndexCarousel(@RequestParam("ids") ids: LongArray, model: Model): String {
        try {
            carouselService.deleteAll(ids.toList())
            model.addAttribute("carouselList", carouselService.findAllEager())

            showInfo("Deleted Carousels with ids ${ids.joinToString()}")
        } catch (e: Exception) {
            logger.error(e.toString(), e)
            showError()
        } finally {
            return DELETE_CAROUSEL
        }
    }

    @PostMapping("/admin/add_date")
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
        model.addAttribute("dateEntity", entity)
        return ADD_DATE
    }

    @GetMapping("/admin/delete_event_date")
    fun refreshEventDates(model : Model) : String {
        model.addAttribute("eventDateList", eventDateService.findAll())
        return DELETE_EVENT_DATE
    }

    @PostMapping("/admin/delete_event_date")
    fun deleteEventdate(@RequestParam("ids") ids: LongArray, model: Model) : String {
        try{
            eventDateService.deleteAll(ids.toList())
            model.addAttribute("eventDateList", eventDateService.findAll())

            showInfo("Deleted Event Dates with ids ${ids.joinToString()}")
        } catch (e : Exception){
            logger.error(e.toString(), e)
            showError()
        } finally {
            return DELETE_EVENT_DATE
        }
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
@Scope("request")
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