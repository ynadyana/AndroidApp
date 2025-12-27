package com.quizapp.tork.model

data class User(var name: String? = null,
                var email: String? = null,
                var password: String? = null,
                var profile: String? = null,
                var points: Long? = 10)
{

    constructor() : this("", "", "","")
}