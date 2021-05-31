package com.example.eyeson.classFile

//테이블의 레코드를 저장할 객체
class RaspberryId {
    var ruuid:String = ""

    //insert,select,delete 생성자
   constructor(ruuid:String){
        this.ruuid = ruuid
    }

    override fun toString(): String {
        return "RaspberryId(ruuid='$ruuid')"
    }
}