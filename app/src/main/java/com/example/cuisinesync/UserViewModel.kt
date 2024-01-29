package com.example.cuisinesync

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.realm.kotlin.mongodb.User
import kotlinx.coroutines.launch

class UserViewModel(private val repository: UserRepository) : ViewModel() {

    private val _registrationStatus = MutableLiveData<Result<User>>()
    val registrationStatus: LiveData<Result<User>> = _registrationStatus

    fun registerUser(userData: UserRepository.UserData) {
        viewModelScope.launch {
            // Here, you're passing the entire userData object to the repository
            _registrationStatus.value = repository.registerUser(userData)
        }
    }


}

class UserViewModelFactory(private val repository: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
