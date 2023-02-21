package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.databinding.FragmentSignInBinding
import ru.netology.nmedia.view.AuthViewModel
import ru.netology.nmedia.view.SignInViewModel

@AndroidEntryPoint
class SignInFragment : Fragment() {

    private val signInViewModel: SignInViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSignInBinding.inflate(inflater, container, false)

        val authViewModel: AuthViewModel by viewModels()

        binding.sighInButton.setOnClickListener {
            signInViewModel.updateUser(
                binding.usedLogin.editText?.text.toString(),
                binding.usedPassword.editText?.text.toString()
            )
        }

        authViewModel.data.observe(viewLifecycleOwner) {
            if (authViewModel.authorized) {
                findNavController().navigateUp()
            }
        }

        return binding.root
    }
}