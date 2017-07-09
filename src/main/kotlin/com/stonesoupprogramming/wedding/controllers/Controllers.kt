package com.stonesoupprogramming.wedding.controllers

import com.stonesoupprogramming.wedding.entities.RoleEntity
import com.stonesoupprogramming.wedding.repositories.RoleRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.validation.Errors
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@Controller
class AdminController(
        @Autowired private val roleRepository: RoleRepository){

    @GetMapping("/admin")
    fun doGet(roleEntity : RoleEntity) : String {
        return "admin"
    }

    @RequestMapping("/delete_roles", method = arrayOf(RequestMethod.POST))
    fun deleteRoles(
            @RequestParam(name = "selectedRoles", required = false)
            ids : LongArray) : String {

        return "admin"
    }

    @PostMapping("/admin")
    fun addRoles(
            @Valid roleEntity: RoleEntity,
            bindingResult: BindingResult) : String {

        return "admin"
    }

    fun populateModel(model: Model) {
        model.apply {
            addAttribute("role", RoleEntity())
            addAttribute("roles", roleRepository.findAll())
        }
    }
}