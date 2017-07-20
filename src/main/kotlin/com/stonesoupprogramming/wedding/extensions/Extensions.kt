package com.stonesoupprogramming.wedding.extensions

import org.springframework.ui.ModelMap
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import java.util.*

fun BindingResult.fail(objectName : String, field: String, key: String, properties: Properties){
    addError(FieldError(objectName, field, properties[key] as String))
}

fun ModelMap.showTab(tabNum: Int){
    this.addAttribute("showTab", tabNum)
}