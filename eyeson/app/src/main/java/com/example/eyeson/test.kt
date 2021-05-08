package com.example.eyeson

fun main(){
    val reg = """[-,0-9]{1,6}""".toRegex()
    val matchResult : MatchResult? = reg.matchEntire("859 - 75번")
    var check : MatchResult? = reg.find("859 - 75번")
    println(matchResult)
    if(matchResult!=null){
        val value5 : String = matchResult!!.value
        println(value5)
    }

    println(check)
    if(check!=null){
        val value : String = check!!.value
        check = check?.next()
        val value2 : String? = check!!.value
        check = check?.next()
        val value3 : String? = check?.value
        check = check?.next()
        val value4 : String? = check?.value
        check = check?.next()
        val value5 : String? = check?.value
        println(value)
        println(value2)
        println(value3)
        println(value4)
        println(value5)
    }

    val check_all : Sequence<MatchResult> = reg.findAll("8597")


}