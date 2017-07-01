package com.stonesoupprogramming.wedding

import javax.persistence.*

@Entity
data class WeddingUser (@field: Id @field: GeneratedValue var id : Int = 0,
                        var userName : String = "",
                        var password : String = "",
                        var enabled : Boolean = false,
                        var roles : MutableSet<Roles> = mutableSetOf())

@Entity
data class Roles(@field: Id @field: GeneratedValue var id : Int = 0,
                 @field: OneToMany var weddingUser: WeddingUser?,
                 var role : String = "")

