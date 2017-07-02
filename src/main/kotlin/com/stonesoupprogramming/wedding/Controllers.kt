package com.stonesoupprogramming.wedding

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMethod

/**
 * Created by stonesoup on 7/2/17.
 */
@Controller
@RequestMapping("/registration")
class RegistrationController(@Autowired val userService: UserService){

    @RequestMapping(method = arrayOf(RequestMethod.GET))
    fun doGet(model : Model) : String {
        model.addAttribute("user", WeddingUser())
        return "registration"
    }

    @RequestMapping(method = arrayOf(RequestMethod.POST))
    fun doPost(weddingUser: WeddingUser) : String {
        userService.saveUser(weddingUser)
        return "registration"
    }
}