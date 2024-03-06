package com.example.cuisinesync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginViewModel(private val dataStoreRepository: DataStoreRepository) : ViewModel() {

    fun saveLoginStatus(isLoggedIn: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepository.saveLoginStatus(isLoggedIn)
        }
    }

    fun saveUserPreferences(username: String, isloggedIn: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepository.saveToDataStore(username, isloggedIn)
        }
    }

    fun clearUserPreferences() {
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepository.clearDataStore()
        }
    }

    suspend fun getLoginStatus(): Boolean {
        return dataStoreRepository.getLoginStatus()
    }

}

class LoginViewModelFactory(
    private val dataStoreRepository: DataStoreRepository)
    : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(dataStoreRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}
