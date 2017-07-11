package com.stonesoupprogramming.wedding.controllers

import com.stonesoupprogramming.wedding.entities.RoleEntity
import com.stonesoupprogramming.wedding.entities.SiteUserEntity
import com.stonesoupprogramming.wedding.repositories.RoleRepository
import com.stonesoupprogramming.wedding.repositories.SiteUserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.annotation.*
import java.util.*
import java.util.function.BooleanSupplier
import javax.validation.Valid

@Controller
class AdminController(
        @Autowired private val roleRepository: RoleRepository,
        @Autowired private val userRepository: SiteUserRepository,
        @Autowired @Qualifier("ValidationProperties") private val validationProperties: Properties
) {

    @GetMapping("/admin")
    fun doGet(model: Model): String {
        populateModel(model)
        return "admin"
    }

    @PostMapping("/admin/delete_roles")
    fun deleteRoles(
            @RequestParam(name = "selectedRoles", required = false) ids: LongArray,
            model: Model): String {
        ids.toSet().forEach { roleRepository.delete(it) }
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
                roleRepository.save(roleEntity)
                modelRole = RoleEntity()
            }
        }
        populateModel(model, roleEntity = modelRole)
        return "admin"
    }

    @PostMapping("/admin/adduser")
    fun addUser(@Valid siteUserEntity: SiteUserEntity,
                @RequestParam(name = "userRoles", required = true) roleIds: LongArray,
                bindingResult: BindingResult,
                model: Model): String {
        var siteUserModel = siteUserEntity

        if (!bindingResult.hasErrors()) {
            if (validate(
                    failCondition = { userRepository.countByUserName(siteUserEntity.userName) > 0L },
                    bindingResult = bindingResult,
                    objectName = "siteUserEntity",
                    field = "userName",
                    message = "user.username.exists"
            ) && validate (
                    failCondition = { userRepository.countByEmail(siteUserEntity.email) > 0L },
                    bindingResult = bindingResult,
                    objectName = "siteUserEntity",
                    field = "email",
                    message = "user.email.exists"
            ) && validate (
                    failCondition = { roleIds.isEmpty() },
                    bindingResult = bindingResult,
                    objectName = "siteUserEntity",
                    field = "roles",
                    message = "user.roles.empty"
            )) {
                val userRoles = roleRepository.findAll(roleIds.toMutableSet()).toMutableSet()
                siteUserEntity.roles = userRoles

                userRepository.save(siteUserEntity)
                siteUserModel = SiteUserEntity()
            }
        }
        populateModel(model = model,
                siteUserEntity = siteUserModel)
        return "admin"
    }

    fun populateModel(model: Model,
                      roleEntity: RoleEntity = RoleEntity(),
                      roleEntities: List<RoleEntity> = roleRepository.findAll(),
                      siteUserEntity: SiteUserEntity = SiteUserEntity()) {
        model.apply {
            addAttribute("userEntity", siteUserEntity)
            addAttribute("roleEntity", roleEntity)
            addAttribute("roleEntities", roleEntities)
        }
    }

    private fun validate(failCondition: () -> Boolean,
                         bindingResult: BindingResult,
                         objectName: String,
                         field: String,
                         message: String): Boolean {
        var pass = true
        if (failCondition.invoke()) {
            bindingResult.addError(FieldError(objectName, field, validationProperties.get(message) as String))
            pass = false
        }
        return pass
    }
}