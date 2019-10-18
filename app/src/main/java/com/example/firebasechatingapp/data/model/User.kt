package com.example.firebasechatingapp.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(var userName:String?, var email:String?,var id:String?, var imageLink:String?):Parcelable{
    constructor():this("","","","")  //empty constructor needed for firebase
}