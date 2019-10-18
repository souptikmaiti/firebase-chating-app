package com.example.firebasechatingapp.data.model

data class Chat(var id:String?, var fromId:String?, var toId:String?, var text: String?, var timeStamp:Long?){
    constructor(): this("","","","", -1)  //empty constructor needed for firebase
}