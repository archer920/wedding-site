package com.stonesoupprogramming.wedding.extensions

import com.stonesoupprogramming.wedding.entities.PersistedFileEntity
import org.springframework.ui.ModelMap
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.multipart.MultipartFile
import java.util.*

fun BindingResult.fail(objectName : String, field: String, key: String, properties: Properties){
    addError(FieldError(objectName, field, properties[key] as String))
}

fun ModelMap.showTab(tabNum: Int){
    this.addAttribute("showTab", tabNum)
}

fun MultipartFile.toPersistedFileEnity(): PersistedFileEntity {
    val persistedFile = PersistedFileEntity(fileName = this.name,
            mime = this.contentType, bytes = this.bytes, size = this.size)
    persistedFile.hash = persistedFile.hashCode()
    return persistedFile
}