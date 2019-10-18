package com.example.firebasechatingapp.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.firebasechatingapp.data.model.User
import com.example.firebasechatingapp.repository.UserRepo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class UserViewModel(private val repo:UserRepo) : ViewModel(){
    var userName:String?=null
    var email:String?=null
    var password:String?=null
    var confirmPass:String?=null
    var imageLink:String?=null
    var userListener:UserListener?=null

    private val compositeDisposables = CompositeDisposable()

    fun registerUser(){
        if(email.isNullOrBlank() || password.isNullOrBlank()){
            userListener?.onFailure("please enter email and password")
            return
        }
        val disposable1 = repo.registerUser(email!!,password!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                userListener?.onRegister("user registered")
            },{
                    userListener?.onFailure(it.message.toString())
                }
            )

        compositeDisposables.add(disposable1)
    }

    fun logInUser(){
        if(email.isNullOrBlank() || password.isNullOrBlank()){
            userListener?.onFailure("please enter email and password")
            return
        }
        val disposable2 = repo.logInUser(email!!,password!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                userListener?.onSuccess("Login Successful")
            },
                {
                    userListener?.onFailure(it.message.toString())
                })

        compositeDisposables.add(disposable2)
    }

    fun addUser(){
        var user = User(userName= userName, email = email, imageLink = imageLink,id = null)
        val disposable3 = repo.addUser(user)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {userListener?.onSuccess("user info saved")},
                {
                    userListener?.onFailure(it.message.toString())
                }
            )
        compositeDisposables.add(disposable3)
    }

    fun updateUser(){
        val disposable6 = repo.updateUser(userName,email)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    userListener?.onSuccess("user updated")
                },{
                    userListener?.onFailure(it.message.toString())
                }
            )
        compositeDisposables.add(disposable6)
    }

    fun addUserPhoto(imageUri: Uri, fileExtension:String){
        val disposable5 = repo.addUserPhoto(imageUri,fileExtension)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    userListener?.onSuccess("image uploaded successfully")
                },{
                    userListener?.onFailure(it.message.toString())
                }
            )
        compositeDisposables.add(disposable5)
    }

    fun getUsers() = repo.getUserData()

    fun getCurrentUser() = repo.getCurrentUser()

    fun signOutUser(){
        repo.signOutUser()
        userListener?.onSuccess("Log Out Successfully")
    }

    fun getUserInfo(uid:String) = repo.getUserInfo(uid)

    fun getLatestMessages(fromId:String) = repo.getLatestMessage(fromId)

    override fun onCleared() {
        super.onCleared()
        compositeDisposables.dispose()
    }
}