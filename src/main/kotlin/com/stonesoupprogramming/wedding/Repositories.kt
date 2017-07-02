package com.stonesoupprogramming.wedding

import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.criterion.Restrictions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
class UserDao(@Autowired val sessionFactory: SessionFactory){

    fun addUser(user: WeddingUser){
        sessionFactory.currentSession.save(user)
    }

    fun findByUserName(userName : String) : WeddingUser =
            sessionFactory.currentSession
                    .createCriteria(WeddingUser::class.java, "wu")
                    .add(Restrictions.eq("wu.userName", userName))
                    .uniqueResult() as WeddingUser
}
