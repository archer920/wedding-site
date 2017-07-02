package com.stonesoupprogramming.wedding

import javax.persistence.*

@Entity
data class WeddingUser (@field: Id @field: GeneratedValue var id : Int = 0,
                        var userName : String = "",
                        var password : String = "",
                        var enabled : Boolean = false,
                        @field: OneToMany(targetEntity = Roles::class) var roles : MutableSet<Roles> = mutableSetOf())

@Entity
data class Roles(@field: Id @field: GeneratedValue var id : Int = 0,
                 @field: ManyToOne(targetEntity = WeddingUser::class) var weddingUser: WeddingUser? = null,
                 var role : String = "")

