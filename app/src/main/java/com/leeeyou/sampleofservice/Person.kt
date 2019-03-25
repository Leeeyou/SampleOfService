package com.leeeyou.sampleofservice

class Person {
    var name: String? = null
    var age: Int = 0

    override fun toString(): String {
        return "Person [name=$name, age=$age]"
    }

    constructor(name: String, age: Int) : super() {
        this.name = name
        this.age = age
    }

    constructor() : super() {}

}
