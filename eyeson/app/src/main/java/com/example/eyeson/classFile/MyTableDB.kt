package com.example.eyeson.classFile

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.widget.Toast

//test.db파일에 저장된 myTable테이블에 CRUD기능을 처리하는 객체
class MyTableDB {
    var contenValObj: ContentValues? = null
    var mydb: SQLiteDatabase? = null
    var context: Context? = null

    constructor(context: Context) {
        this.context = context
        var helper = DBHelper(context)
        mydb = helper.writableDatabase
    }

    fun insert(raspberryId: RaspberryId) {
        //데이터저장하기
        //매개변수로 전달된 Person객체에 저장된 데이터를 SQLite에 insert하기
        contenValObj = ContentValues()
        contenValObj!!.put("ruuid", raspberryId.ruuid)
        //insert내부에서 insert기능을 처리할 수 있는 sql문을 생성한다. 이때 필요한 컬럼에 전달할 값 등은 ContentValues에서 추출
        mydb?.insert("mytable", null, contenValObj)
    }

    fun select(): ArrayList<RaspberryId> {
        var datalist: ArrayList<RaspberryId>? = ArrayList<RaspberryId>()
        /*
            select 컬럼명 from 테이블명 where 조건
            group by 컬럼명
            having 조건
            order by 컬럼명
            1. table매개변수 -> 테이블명
            2. columns -> 조회할 컬럼명 배열
            3. selection -> 조건(where절 다음에 오는 문자열 : id = ?)
            4. selectionArgs -> 조건으로 정의된 ?에 바인딩될 데이터 배열로 처리
            5. groupby -> group by절 뒤에 정의할 컬럼명
            6. having -> having절 뒤에 정의할 조건
            7. orderby -> order by절에 정의할 정렬기준
         */
        val cursor = mydb?.query("mytable", null, null, null, null, null, null)
        val count = cursor?.count // 레코드갯수반환
//        Toast.makeText(context,"조회된 row:$count",Toast.LENGTH_LONG).show()
        //조회된 데이터를 ContentValuesTestActivity로 넘기기위해 ArrayList로 변환
        while (cursor!!.moveToNext()) {
            //DB에서 조회된 레코드로 Person객체를 생성
            var raspberryId = RaspberryId(
                cursor.getString(0)
            )
            datalist!!.add(raspberryId)
        }
        return datalist!!
    }

    fun delete(ruuid: String) {
        //whereArgs는 whereclause에서 조건에 정의한 ?의 갯수만큼배열로 작성해야 한다.
        mydb?.delete("mytable", "ruuid=?", arrayOf(ruuid))

    }
}