package com.example.firebasechatingapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.firebasechatingapp.data.model.Chat
import com.example.firebasechatingapp.repository.UserRepo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*

class ChatViewModel(private val repo: UserRepo): ViewModel() {
    var fromId:String? = repo.getCurrentUser()?.uid
    var toId:String? = null
    var msg: String? = null
    val compositeDisposable = CompositeDisposable()
    var chatListener: ChatListener?=null

    fun addChat(){
        val chatId = UUID.randomUUID().toString()
        var chat = Chat(id=chatId,fromId = fromId, toId = toId,text = msg, timeStamp = System.currentTimeMillis()/1000)
        val disposable = repo.addChat(chat)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    addToLatestMessage(chat)
                    chatListener?.onSuccess("message sent")
                },{
                    chatListener?.onFailure(it.message.toString())
                }
            )
        compositeDisposable.add(disposable)
    }

    fun addToLatestMessage(chat: Chat){
        val disposable = repo.addLatestMessage(chat)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {

                },{

                }
            )
        compositeDisposable.add(disposable)
    }

    fun getChats() = repo.getChats(fromId!!,toId!!)

    fun getChatAdded() = repo.getChatAdded(fromId!!,toId!!)

    fun getUserInfo(uid:String) = repo.getUserInfo(uid)

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}