package com.example.firebasechatingapp.viewmodel

interface UserListener {
    fun onSuccess(msg:String)
    fun onFailure(msg:String)
    fun onRegister(msg: String)
}