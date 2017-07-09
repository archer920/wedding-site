package com.stonesoupprogramming.wedding.controllers

import com.stonesoupprogramming.wedding.entities.RoleEntity
import com.stonesoupprogramming.wedding.repositories.RoleRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid

@Controller
class AdminController(
        @Autowired private val roleRepository: RoleRepository,
        @Autowired @Qualifier("ValidationProperties") private val validationProperties : Properties
        ){

    @GetMapping("/admin")
    fun doGet(model: Model) : String {
        populateModel(model)
        return "admin"
    }

    @PostMapping("/admin/delete_roles")
    fun deleteRoles(
            @RequestParam(name = "selectedRoles", required = false) ids : LongArray,
            model: Model) : String {
        ids.toSet().forEach { roleRepository.delete(it) }
        populateModel(model)
        return "admin"
    }

    @PostMapping("/admin/addrole")
    fun addRoles(
            @Valid roleEntity: RoleEntity,
            bindingResult: BindingResult,
            model: Model) : String {
        var modelRole = roleEntity

        if(!bindingResult.hasErrors()){
            if(roleRepository.countByRole(roleEntity.role) == 0L){
                roleRepository.save(roleEntity)
                modelRole = RoleEntity()
            } else {
                bindingResult.addError(FieldError("roleEntity", "role", validationProperties.getValue("role.name.duplicate") as String))
            }
        }
        populateModel(model, roleEntity=modelRole)
        return "admin"
    }

    fun populateModel(model: Model,
                      roleEntity: RoleEntity = RoleEntity(),
                      roleEntities : List<RoleEntity> = roleRepository.findAll()) {
        model.apply {
            addAttribute("roleEntity", roleEntity)
            addAttribute("roleEntities", roleEntities)
        }
    }
}