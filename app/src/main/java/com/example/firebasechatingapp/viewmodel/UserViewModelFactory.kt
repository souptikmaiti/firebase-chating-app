package com.example.firebasechatingapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.firebasechatingapp.repository.UserRepo

@Suppress("UNCHECKED_CAST")
class UserViewModelFactory(private val repo:UserRepo): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return UserViewModel(repo) as T
    }
}