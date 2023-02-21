package ru.netology.nmedia.view


import android.net.Uri
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import ru.netology.nmedia.viewmodel.PhotoModel
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton


private val noPhoto = PhotoModel(null, null)

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class SignUpViewModel @Inject constructor(
    private val repository: SignUpRepository,
    appAuth: AppAuth,
) : ViewModel() {


    val state = appAuth.state
        .asLiveData()
    val authorized: Boolean
        get() = state.value != null

    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<PhotoModel>
        get() = _photo

    fun updateUser(login: String, pass: String, name: String, file: File) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                repository.update(login, pass, name, file)
            } catch (e: java.io.IOException) {
                throw NetworkError
            } catch (e: Exception) {
                throw UnknownError
            }
        }
    }


    fun changePhoto(uri: Uri?, file: File?) {
        _photo.value = PhotoModel(uri, file)
    }
}

@Singleton
class SignUpRepository@Inject constructor
    (
    private val apiService: ApiService,
    private val appAuth: AppAuth,
) {

    suspend fun update(login: String, pass: String, name: String, file: File) {

        try {
            val data = MultipartBody.Part.createFormData(
                "file", file.name, file.asRequestBody()
            )
            val response = apiService.registerWithPhoto(
                login.toRequestBody("text/plain".toMediaType()),
                pass.toRequestBody("text/plain".toMediaType()),
                name.toRequestBody("text/plain".toMediaType()),
                data
            )
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
