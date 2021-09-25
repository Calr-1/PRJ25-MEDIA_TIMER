package com.example.mediatimerjp.database

class User {
    var name: String = ""
    private var email: String = ""
    var timers: ArrayList<String> = ArrayList()


    constructor()


    constructor(name: String, email: String, timers:ArrayList<String>) {
        this.name = name
        this.email = email
        this.timers = timers
    }
}