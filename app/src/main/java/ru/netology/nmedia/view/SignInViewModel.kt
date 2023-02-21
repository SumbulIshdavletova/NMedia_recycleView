package ru.netology.nmedia.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SignInViewModel @Inject constructor(
    private val repository: AuthRepository,
    appAuth: AppAuth,
) : ViewModel() {


    val state = appAuth.state
        .asLiveData()
    val authorized: Boolean
        get() = state.value != null

    fun updateUser(login: String, pass: String) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                repository.update(login, pass)
            } catch (e: java.io.IOException) {
                throw NetworkError
            } catch (e: Exception) {
                throw UnknownError
            }
        }
    }
}

@Singleton
class AuthRepository @Inject constructor
    (
    private val apiService: ApiService,
    private val appAuth: AppAuth
) {

    suspend fun update(login: String, pass: String) {
        try {
            val response = apiService.updateUser(login, pass)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            body.token?.let { appAuth.setAuth(body.id, it) }
        } catch (e: java.io.IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}

