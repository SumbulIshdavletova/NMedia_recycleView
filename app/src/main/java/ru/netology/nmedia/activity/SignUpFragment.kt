package ru.netology.nmedia.activity

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.databinding.FragmentSignUpBinding
import ru.netology.nmedia.view.AuthViewModel
import ru.netology.nmedia.view.SignUpViewModel

@AndroidEntryPoint
class SignUpFragment : Fragment() {

    private val signUpViewModel: SignUpViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSignUpBinding.inflate(inflater, container, false)

        val authViewModel: AuthViewModel by viewModels()


        authViewModel.data.observe(viewLifecycleOwner) {
            if (authViewModel.authorized) {
                findNavController().navigateUp()
            }
        }


        val contract =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                val resultCode = result.resultCode
                val data = result.data

                when (resultCode) {
                    ImagePicker.RESULT_ERROR -> {
                        Snackbar.make(
                            binding.root,
                            ImagePicker.getError(data),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                    Activity.RESULT_OK -> {
                        val fileUri = data?.data

                        signUpViewModel.changePhoto(fileUri, fileUri?.toFile())

                    }
                }
            }

        binding.addAvatar.setOnClickListener {
            ImagePicker.with(this)
                .cropSquare()
                .compress(500)
                .createIntent(contract::launch)
        }


        binding.sighUpButton.setOnClickListener {
            signUpViewModel.photo.value?.file?.let { it1 ->
                signUpViewModel.updateUser(
                    binding.userLogin.text.toString(),
                    binding.userPassword.text.toString(),
                    binding.userPasswordConfirm.text.toString(),
                    it1
                )
            }
        }


        return binding.root
    }
}