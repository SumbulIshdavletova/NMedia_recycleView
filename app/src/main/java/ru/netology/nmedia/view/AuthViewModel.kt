package ru.netology.nmedia.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.auth.AuthState
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModel @Inject constructor(
    private val auth: AppAuth
) : ViewModel() {

    val data: LiveData<AuthState?> = auth.state.asLiveData(Dispatchers.Default)

//    val authentication: MutableLiveData<Boolean> by lazy {
//        MutableLiveData<Boolean>()
//    }

    val authorized: Boolean
        get() = auth.state.value?.id != 0L

}
