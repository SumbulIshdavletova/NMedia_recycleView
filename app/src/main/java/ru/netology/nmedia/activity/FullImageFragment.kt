package ru.netology.nmedia.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentFullImageBinding
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.viewmodel.PostViewModel


@AndroidEntryPoint
class FullImageFragment : Fragment() {

    companion object {
        var Bundle.textArg: String? by StringArg
    }

    private var _binding: FragmentFullImageBinding? = null
    private val binding get() = _binding!!

    @OptIn(ExperimentalCoroutinesApi::class)
    private val viewModel: PostViewModel by viewModels()

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFullImageBinding.inflate(
            inflater, container, false
        )

        lifecycleScope.launchWhenCreated {
            viewModel.data.collectLatest {
                val name = arguments?.textArg
                val url = "${BuildConfig.BASE_URL}/media/${name}"
                Glide.with(binding.fullImage)
                    .load(url)
                    .placeholder(R.drawable.ic_baseline_rotate_right_24)
                    .error(R.drawable.ic_baseline_error_24)
                    .timeout(10_000)
                    .into(binding.fullImage)
            }
        }

//        viewModel.data.observe(viewLifecycleOwner) {
//            val name = arguments?.textArg
//            val url = "${BuildConfig.BASE_URL}/media/${name}"
//            Glide.with(binding.fullImage)
//                .load(url)
//                .placeholder(R.drawable.ic_baseline_rotate_right_24)
//                .error(R.drawable.ic_baseline_error_24)
//                .timeout(10_000)
//                .into(binding.fullImage)
//        }


        return binding.root
    }
}