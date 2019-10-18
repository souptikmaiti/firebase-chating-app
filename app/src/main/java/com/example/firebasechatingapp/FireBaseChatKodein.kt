package com.example.firebasechatingapp

import android.app.Application
import com.example.firebasechatingapp.data.firebase.FirebaseSource
import com.example.firebasechatingapp.repository.UserRepo
import com.example.firebasechatingapp.viewmodel.ChatViewModelFactory
import com.example.firebasechatingapp.viewmodel.UserViewModelFactory
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton

class FireBaseChatKodein: Application(), KodeinAware {

    override val kodein: Kodein = Kodein.lazy {
        import(androidXModule(this@FireBaseChatKodein))

        bind() from singleton { FirebaseSource() }
        bind() from singleton { UserRepo(instance()) }
        bind() from provider { UserViewModelFactory(instance()) }
        bind() from provider { ChatViewModelFactory(instance()) }
    }
}