package com.stonesoupprogramming.wedding.controllers

import com.stonesoupprogramming.wedding.entities.RoleEntity
import com.stonesoupprogramming.wedding.extensions.fail
import com.stonesoupprogramming.wedding.extensions.showTab
import com.stonesoupprogramming.wedding.services.CarouselService
import com.stonesoupprogramming.wedding.services.RoleService
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.ModelMap
import org.springframework.validation.BindingResult
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

    fun showError()
    fun showError(error: String)
    fun showError(errorList: List<String>)

    fun showInfo(info : String)
    fun showInfo(infoList : List<String>)
}

@Component
@Scope("prototype")
@Primary
class UiMessageHandlerImpl(
        @Autowired @Qualifier("ValidationProperties")
        private var validationProperties : Properties) : UiMessageHandler {

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
}

interface BannerAttributes {

    @ModelAttribute
    fun navBarLinks(model : ModelMap)
}

@Component
@Scope("prototype")
class BannerAttributesImpl(
        @Autowired private val carouselService: CarouselService) : BannerAttributes{

    //Do not handle error. Show the 500 page instead
    override fun navBarLinks(model : ModelMap){
        model.addAttribute("navbarLinks", carouselService.findAll())
    }
}

interface DestinationHolder {

    fun destination() : String

    fun redirect() : String
}

@Component
@Scope("prototype")
class DestinationHolderImpl(private val destination : String = "admin",
                            private val redirect : String = "redirect:/admin") : DestinationHolder {

    override fun redirect(): String = redirect

    override fun destination(): String  = destination

}

interface RoleController {

    @ModelAttribute("allRoles")
    fun allRoles() : List<RoleEntity>

    @ModelAttribute("roleEntity")
    fun entity() : RoleEntity

    @PostMapping("/admin/delete_roles")
    fun deleteSelectedRoles(@RequestParam(name = "ids") ids: LongArray, model : ModelMap) : String

    @PostMapping("/admin/add_role")
    fun addRole(@ModelAttribute @Valid roleEntity: RoleEntity, bindingResult: BindingResult, model : ModelMap) : String
}

@Component
@Scope("prototype")
class RoleControllerImpl(
        @Autowired private val logger : Logger,
        @Autowired private val rolesService: RoleService,
        @Autowired private val destinationHolder: DestinationHolder,
        @Autowired @Qualifier("ValidationProperties") private val validationProperties: Properties,
        @Autowired private val uiMessageHandler: UiMessageHandler) : RoleController {

    private val TAB_NUM = 0
    private var entity = RoleEntity()

    override fun deleteSelectedRoles(ids: LongArray, model: ModelMap): String {
        try {
            rolesService.deleteAll(ids.asList())
        } catch (e : Exception){
            logger.error(e.toString(), e)
            uiMessageHandler.showError("Error while deleting selected roles. Please try again")
        } finally {
            model.showTab(TAB_NUM)
            return destinationHolder.redirect()
        }
    }

    override fun addRole(roleEntity: RoleEntity, bindingResult: BindingResult, model: ModelMap): String {
        var outcome : String = destinationHolder.destination()

        if(!bindingResult.hasErrors()){
            if(rolesService.countByRole(roleEntity.role) == 0L){
                try {
                    rolesService.save(roleEntity)
                    uiMessageHandler.showInfo("Role ${roleEntity.role} has been added")
                    outcome = destinationHolder.redirect()
                } catch (e : Exception){
                    logger.error(e.toString(), e)
                    uiMessageHandler.showError()
                }
            } else {
                bindingResult.fail("roleEntity", "role", "role.name.duplicate", validationProperties)
            }
        }
        model.showTab(TAB_NUM)
        return outcome
    }

    override fun entity(): RoleEntity = entity

    override fun allRoles() = rolesService.findAll().toList()
}

@Controller
@Scope("request")
class AdminController (
        @Autowired private val roleController: RoleController,
        @Autowired private val uiMessageHandler: UiMessageHandler)
    : RoleController by roleController, UiMessageHandler by uiMessageHandler {

    @GetMapping("/admin")
    fun doGet(model: ModelMap): String {
        return "admin"
    }

//    @PostMapping("/admin/adduser")
//    fun addUser(@Valid siteUserEntity: SiteUserEntity,
//                bindingResult: BindingResult,
//                model: Model): String {
//        var siteUserModel = siteUserEntity
//
//        if (!bindingResult.hasErrors()) {
//            if (validate(
//                    failCondition = { siteUserService.siteUserRepository.countByUserName(siteUserEntity.userName) > 0L },
//                    bindingResult = bindingResult,
//                    objectName = "siteUserEntity",
//                    field = "userName",
//                    message = "user.username.exists"
//            ) && validate (
//                    failCondition = { siteUserService.siteUserRepository.countByEmail(siteUserEntity.email) > 0L },
//                    bindingResult = bindingResult,
//                    objectName = "siteUserEntity",
//                    field = "email",
//                    message = "user.email.exists"
//            ) && validate (
//                    failCondition = { !siteUserEntity.passwordMatch() },
//                    bindingResult = bindingResult,
//                    objectName = "siteUserEntity",
//                    field = "validatePassword",
//                    message = "user.passwords.nomatch"
//            )){
//                val roles = roleRepository.findAll(siteUserEntity.roleIds.toMutableList()).toMutableSet()
//                siteUserEntity.roles = roles
//
//                try {
//                    siteUserService.save(siteUserEntity)
//                    //messageHandler.infoMsgs.add("Added user ${siteUserEntity.userName} successfully")
//                    siteUserModel = SiteUserEntity()
//                } catch (e: Exception) {
//                    logger.error(e.toString(), e)
//                    messageHandler.showError()
//                }
//            }
//        }
//        populateModel(model = model,
//                showTab = 1,
//                siteUserEntity = siteUserModel)
//        return "admin"
//    }
//
//    @PostMapping("/admin/delete_users")
//    fun deleteUsers(
//            @RequestParam("userIds", required = true)
//            ids : LongArray, model : Model) : String {
//        try{
//            siteUserService.deleteAll(ids.toList())
//            //messageHandler.infoMsgs.add("Deleted the following users ${ids.joinToString()}")
//        } catch (e : Exception){
//            logger.error(e.toString(), e)
//            messageHandler.showError()
//        } finally {
//            populateModel(model = model,
//                    showTab = 1)
//            return "admin"
//        }
//    }
//
//    @PostMapping("/admin/file_upload")
//    fun addPersistedFile(
//            @RequestPart("file") multipartFile: MultipartFile,
//            model: Model) : String {
//        try {
//            persistedFileService.save(multipartFile)
//            //messageHandler.infoMsgs.add("Uploaded ${multipartFile.name}")
//        } catch (e : Exception){
//            //messageHandler.errorMsgs.add("Failed to upload ${multipartFile.name}")
//            logger.error(e.toString(), e)
//        } finally {
//            populateModel(model = model,
//                    showTab = 2)
//            return "admin"
//        }
//    }
//
//    @PostMapping("/admin/file_delete")
//    fun deletePersistedFile(
//            @RequestParam("fileIds") ids: LongArray,
//            model: Model) : String {
//        try {
//            //persistedFileService.deleteAll(ids)
//            //messageHandler.infoMsgs.add("Deleted files ${ids.joinToString()}")
//        } catch (e : Exception){
//            //messageHandler.errorMsgs.add("Failed to delete files ${ids.joinToString()}")
//            logger.error(e.toString(), e)
//        } finally {
//            populateModel(model = model,
//                    showTab = 2)
//            return "admin"
//        }
//    }
//
//    @PostMapping("/admin/carousel/delete")
//    fun deleteCarouselEntities(
//            @RequestParam("carouselIds") ids: LongArray,
//            model : Model) : String {
//        try{
//            //carouselService.delete(ids)
//            //messageHandler.infoMsgs.add("Deleted Carousels ids ${ids.joinToString()}")
//        } catch (e : Exception){
//            logger.error(e.toString(), e)
//            messageHandler.showError()
//        } finally {
//            populateModel(model = model,
//                showTab = 3)
//            return "admin"
//        }
//    }
//
//    @PostMapping("/admin/carousel/add")
//    fun addCarouselEntity(@Valid carouselEntity: CarouselEntity,
//                          bindingResult: BindingResult,
//                          model : Model) : String {
//        var carouselReturn = carouselEntity
//        try {
//            if(!bindingResult.hasErrors()){
//                carouselService.save(carouselEntity)
//                //messageHandler.infoMsgs.add("Carousel has been saved")
//                carouselReturn = CarouselEntity()
//            }
//        } catch (e : Exception){
//            logger.error(e.toString(), e)
//        } finally {
//            populateModel(showTab = 3,
//                    carouselEntity = carouselReturn,
//                    model = model)
//            return "admin"
//        }
//    }
//
    fun showTab(tabNum : Int, model: Model){
        model.addAttribute("showTab", tabNum)
    }
//
//    fun populateModel(model: Model,
//                      showTab : Int = 0,
//                      roleEntity: RoleEntity = RoleEntity(),
//                      roleEntities: List<RoleEntity> = roleRepository.findAll(),
//                      siteUserEntity: SiteUserEntity = SiteUserEntity(),
//                      carouselEntity: CarouselEntity = CarouselEntity(),
//                      siteUserEntities : List<SiteUserEntity> = siteUserService.siteUserRepository.findAll(),
//                      persistedFileEntities : List<PersistedFileEntity> = persistedFileService.findAll(),
//                      carouselEntities : List<CarouselEntity> = carouselService.findAllEager().toList()) {
//        model.apply {
//            addAttribute("showTab", showTab)
//            addAttribute("siteUserEntity", siteUserEntity)
//            addAttribute("roleEntity", roleEntity)
//            addAttribute("carouselEntity", carouselEntity)
//            addAttribute("roleEntities", roleEntities)
//            addAttribute("siteUserEntities", siteUserEntities)
//            addAttribute("persistedFileEntities", persistedFileEntities)
//            addAttribute("carouselEntities", carouselEntities)
//        }
//        //messageHandler.populateMessages(model)
//    }
//
//    private fun validate(failCondition: () -> Boolean,
//                         bindingResult: BindingResult,
//                         objectName: String,
//                         field: String,
//                         message: String): Boolean {
//        var pass = true
//        if (failCondition.invoke()) {
//            bindingResult.addError(FieldError(objectName, field, validationProperties[message] as String))
//            pass = false
//        }
//        return pass
//    }
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