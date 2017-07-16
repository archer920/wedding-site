package com.stonesoupprogramming.wedding.controllers

import com.stonesoupprogramming.wedding.entities.CarouselEntity
import com.stonesoupprogramming.wedding.entities.PersistedFileEntity
import com.stonesoupprogramming.wedding.entities.RoleEntity
import com.stonesoupprogramming.wedding.entities.SiteUserEntity
import com.stonesoupprogramming.wedding.repositories.RoleRepository
import com.stonesoupprogramming.wedding.repositories.SiteUserRepository
import com.stonesoupprogramming.wedding.services.CarouselService
import com.stonesoupprogramming.wedding.services.PersistedFileService
import com.stonesoupprogramming.wedding.services.SiteUserService
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Scope
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.*
import javax.validation.Valid

@Component
@Scope("prototype")
class UiMessageHandler(
        val errorMsgs: MutableSet<String> = mutableSetOf(),
        val infoMsgs: MutableSet<String> = mutableSetOf(),

        @field: Autowired
        @field: Qualifier("ValidationProperties")
        private var validationProperties : Properties? = null) {

    fun populateMessages(model: Model){
        model.apply {
            model.addAttribute("errors", errorMsgs.toList())
            model.addAttribute("info", infoMsgs.toList())
        }
    }

    fun generalServerError(){
        errorMsgs.add(validationProperties?.getProperty("general.server.error") ?: "")
    }
}

interface BannerAttributes {

    @ModelAttribute
    fun navBarLinks(model : Model)
}

@Component
@Scope("prototype")
class BannerAttributesImpl(@Autowired private val carouselService: CarouselService) : BannerAttributes{

    @ModelAttribute
    override fun navBarLinks(model : Model){
        model.addAttribute("navbarLinks", carouselService.findAll())
    }
}

@Controller
@Scope("request")
class AdminController (
        @Autowired private val logger : Logger,
        @Autowired private val roleRepository: RoleRepository,
        @Autowired private val siteUserService : SiteUserService,
        @Autowired @Qualifier("ValidationProperties") private val validationProperties: Properties,
        @Autowired private val messageHandler: UiMessageHandler,
        @Autowired private val persistedFileService: PersistedFileService,
        @Autowired private val carouselService: CarouselService) {

    @GetMapping("/admin")
    fun doGet(model: Model): String {
        populateModel(model)
        return "admin"
    }

    @PostMapping("/admin/delete_roles")
    fun deleteRoles(
            @RequestParam(name = "selectedRoles", required = false) ids: LongArray,
            model: Model): String {

        val successfulDeletes : MutableList<String> = mutableListOf()
        val deletedRoles = roleRepository.findAll(ids.toList())

        ids.toSet().forEach { id ->
            try {
                roleRepository.delete(id)
                successfulDeletes.add(
                        deletedRoles.find { it.id == id }?.role ?: id.toString())
            } catch (e : Exception){
                when (e) {
                    is DataIntegrityViolationException -> messageHandler.errorMsgs.add("Role ID is used as a foreign key")
                    else  -> messageHandler.generalServerError()
                }
                logger.error(e.toString(), e)
            }
        }

        if(successfulDeletes.isNotEmpty()){
            messageHandler.infoMsgs.add("Deleted roles ${successfulDeletes.joinToString(",")}")
        }
        populateModel(model)
        return "admin"
    }

    @PostMapping("/admin/addrole")
    fun addRoles(
            @Valid roleEntity: RoleEntity,
            bindingResult: BindingResult,
            model: Model): String {
        var modelRole = roleEntity

        if (!bindingResult.hasErrors()) {
            if (validate(
                    failCondition = { roleRepository.countByRole(roleEntity.role) > 0L },
                    bindingResult = bindingResult,
                    objectName = "roleEntity",
                    field = "role",
                    message = "role.name.duplicate")) {

                try {
                    roleRepository.save(roleEntity)
                    messageHandler.infoMsgs.add("Role ${roleEntity.role} added successfully")
                    modelRole = RoleEntity()
                } catch (e : Exception){
                    logger.error(e.toString(), e)
                    messageHandler.generalServerError()
                }

            }
        }
        populateModel(model, roleEntity = modelRole)
        return "admin"
    }

    @PostMapping("/admin/adduser")
    fun addUser(@Valid siteUserEntity: SiteUserEntity,
                bindingResult: BindingResult,
                model: Model): String {
        var siteUserModel = siteUserEntity

        if (!bindingResult.hasErrors()) {
            if (validate(
                    failCondition = { siteUserService.siteUserRepository.countByUserName(siteUserEntity.userName) > 0L },
                    bindingResult = bindingResult,
                    objectName = "siteUserEntity",
                    field = "userName",
                    message = "user.username.exists"
            ) && validate (
                    failCondition = { siteUserService.siteUserRepository.countByEmail(siteUserEntity.email) > 0L },
                    bindingResult = bindingResult,
                    objectName = "siteUserEntity",
                    field = "email",
                    message = "user.email.exists"
            ) && validate (
                    failCondition = { !siteUserEntity.passwordMatch() },
                    bindingResult = bindingResult,
                    objectName = "siteUserEntity",
                    field = "validatePassword",
                    message = "user.passwords.nomatch"
            )){
                val roles = roleRepository.findAll(siteUserEntity.roleIds.toMutableList()).toMutableSet()
                siteUserEntity.roles = roles

                try {
                    siteUserService.save(siteUserEntity)
                    messageHandler.infoMsgs.add("Added user ${siteUserEntity.userName} successfully")
                    siteUserModel = SiteUserEntity()
                } catch (e: Exception) {
                    logger.error(e.toString(), e)
                    messageHandler.generalServerError()
                }
            }
        }
        populateModel(model = model,
                showTab = 1,
                siteUserEntity = siteUserModel)
        return "admin"
    }

    @PostMapping("/admin/delete_users")
    fun deleteUsers(
            @RequestParam("userIds", required = true)
            ids : LongArray, model : Model) : String {
        try{
            siteUserService.deleteAll(ids.toList())
            messageHandler.infoMsgs.add("Deleted the following users ${ids.joinToString()}")
        } catch (e : Exception){
            logger.error(e.toString(), e)
            messageHandler.generalServerError()
        } finally {
            populateModel(model = model,
                    showTab = 1)
            return "admin"
        }
    }

    @PostMapping("/admin/file_upload")
    fun addPersistedFile(
            @RequestPart("file") multipartFile: MultipartFile,
            model: Model) : String {
        try {
            persistedFileService.save(multipartFile)
            messageHandler.infoMsgs.add("Uploaded ${multipartFile.name}")
        } catch (e : Exception){
            messageHandler.errorMsgs.add("Failed to upload ${multipartFile.name}")
            logger.error(e.toString(), e)
        } finally {
            populateModel(model = model,
                    showTab = 2)
            return "admin"
        }
    }

    @PostMapping("/admin/file_delete")
    fun deletePersistedFile(
            @RequestParam("fileIds") ids: LongArray,
            model: Model) : String {
        try {
            persistedFileService.deleteAll(ids)
            messageHandler.infoMsgs.add("Deleted files ${ids.joinToString()}")
        } catch (e : Exception){
            messageHandler.errorMsgs.add("Failed to delete files ${ids.joinToString()}")
            logger.error(e.toString(), e)
        } finally {
            populateModel(model = model,
                    showTab = 2)
            return "admin"
        }
    }

    @PostMapping("/admin/carousel/delete")
    fun deleteCarouselEntities(
            @RequestParam("carouselIds") ids: LongArray,
            model : Model) : String {
        try{
            carouselService.delete(ids)
            messageHandler.infoMsgs.add("Deleted Carousels ids ${ids.joinToString()}")
        } catch (e : Exception){
            logger.error(e.toString(), e)
            messageHandler.generalServerError()
        } finally {
            populateModel(model = model,
                showTab = 3)
            return "admin"
        }
    }

    @PostMapping("/admin/carousel/add")
    fun addCarouselEntity(@Valid carouselEntity: CarouselEntity,
                          bindingResult: BindingResult,
                          model : Model) : String {
        var carouselReturn = carouselEntity
        try {
            if(!bindingResult.hasErrors()){
                carouselService.save(carouselEntity)
                messageHandler.infoMsgs.add("Carousel has been saved")
                carouselReturn = CarouselEntity()
            }
        } catch (e : Exception){
            logger.error(e.toString(), e)
        } finally {
            populateModel(showTab = 3,
                    carouselEntity = carouselReturn,
                    model = model)
            return "admin"
        }
    }

    fun populateModel(model: Model,
                      showTab : Int = 0,
                      roleEntity: RoleEntity = RoleEntity(),
                      roleEntities: List<RoleEntity> = roleRepository.findAll(),
                      siteUserEntity: SiteUserEntity = SiteUserEntity(),
                      carouselEntity: CarouselEntity = CarouselEntity(),
                      siteUserEntities : List<SiteUserEntity> = siteUserService.siteUserRepository.findAll(),
                      persistedFileEntities : List<PersistedFileEntity> = persistedFileService.findAll(),
                      carouselEntities : List<CarouselEntity> = carouselService.findAllEager().toList()) {
        model.apply {
            addAttribute("showTab", showTab)
            addAttribute("siteUserEntity", siteUserEntity)
            addAttribute("roleEntity", roleEntity)
            addAttribute("carouselEntity", carouselEntity)
            addAttribute("roleEntities", roleEntities)
            addAttribute("siteUserEntities", siteUserEntities)
            addAttribute("persistedFileEntities", persistedFileEntities)
            addAttribute("carouselEntities", carouselEntities)
        }
        messageHandler.populateMessages(model)
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
        private val carouselService: CarouselService,

        @Autowired
        private val bannerAttributes: BannerAttributes) : BannerAttributes by bannerAttributes {

    @GetMapping("/")
    fun doGet(model : Model) : String {
        model.addAttribute("carousels",
                carouselService.findAllEager())
        return "index"
    }
}