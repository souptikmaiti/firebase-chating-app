package com.example.firebasechatingapp.repository

import android.net.Uri
import com.example.firebasechatingapp.data.firebase.FirebaseSource
import com.example.firebasechatingapp.data.model.Chat
import com.example.firebasechatingapp.data.model.User

class UserRepo(private val firebaseSource: FirebaseSource) {

    fun registerUser(email:String,pwd:String) = firebaseSource.registerUser(email,pwd)

    fun logInUser(email:String,pwd:String) = firebaseSource.logInUser(email,pwd)

    fun getCurrentUser() = firebaseSource.getCurrentUser()

    fun getUserInfo(uid:String) = firebaseSource.getUserInfo(uid)

    fun signOutUser() = firebaseSource.signOutUser()

    fun getUserData() = firebaseSource.getUserData()

    fun addUser(user: User) = firebaseSource.addUser(user)

    fun updateUser(username:String?, email:String?) = firebaseSource.updateUser(username,email)

    fun addUserPhoto(imageUri: Uri, fileExtension:String) = firebaseSource.addPhotoToStorage(imageUri,fileExtension)

    fun addChat(chat: Chat) = firebaseSource.addChat(chat)

    fun addLatestMessage(chat: Chat) = firebaseSource.addToLatestMessage(chat)

    fun getLatestMessage(fromId: String) = firebaseSource.getLatestMessage(fromId)

    fun getChats(fromId:String, toId:String) = firebaseSource.getChat(fromId,toId)

    fun getChatAdded(fromId:String, toId:String) = firebaseSource.getChatAdded(fromId,toId)
}