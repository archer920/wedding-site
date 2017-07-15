package com.stonesoupprogramming.wedding.controllers

import com.stonesoupprogramming.wedding.entities.RoleEntity
import com.stonesoupprogramming.wedding.entities.SiteUserEntity
import com.stonesoupprogramming.wedding.repositories.RoleRepository
import com.stonesoupprogramming.wedding.repositories.SiteUserRepository
import com.stonesoupprogramming.wedding.services.SiteUserService
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Scope
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
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

@Controller
@Scope("request")
class AdminController (
        @Autowired private val logger : Logger,
        @Autowired private val roleRepository: RoleRepository,
        @Autowired private val siteUserService : SiteUserService,
        @Autowired @Qualifier("ValidationProperties") private val validationProperties: Properties,
        @Autowired private val messageHandler: UiMessageHandler) {

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

    fun populateModel(model: Model,
                      showTab : Int = 0,
                      roleEntity: RoleEntity = RoleEntity(),
                      roleEntities: List<RoleEntity> = roleRepository.findAll(),
                      siteUserEntity: SiteUserEntity = SiteUserEntity(),
                      siteUserEntities : List<SiteUserEntity> = siteUserService.siteUserRepository.findAll()) {
        model.apply {
            addAttribute("showTab", showTab)
            addAttribute("siteUserEntity", siteUserEntity)
            addAttribute("roleEntity", roleEntity)
            addAttribute("roleEntities", roleEntities)
            addAttribute("siteUserEntities", siteUserEntities)
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