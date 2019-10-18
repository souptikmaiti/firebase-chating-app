package com.example.firebasechatingapp.viewmodel

interface ChatListener {
    fun onSuccess(msg:String)
    fun onFailure(msg:String)
}