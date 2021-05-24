package com.example.eyeson.dataFile
import android.os.Parcel
import android.os.Parcelable

//안드로이드에서는 객체를 인텐트에 공유하고 싶으면 Parcelable타입으로 정의
//자동으로 메소드가 오버라이딩되고 생성자가 추가
class UUID_Parcelable() : Parcelable {
    var uu_id:String? = ""

    constructor(parcel: Parcel) : this() {
        uu_id = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(uu_id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UUID_Parcelable> {
        override fun createFromParcel(parcel: Parcel): UUID_Parcelable {
            val obj = UUID_Parcelable()
            obj.uu_id = parcel.readString()
            return obj
        }

        override fun newArray(size: Int): Array<UUID_Parcelable?> {
            return arrayOfNulls(size)
        }
    }
}