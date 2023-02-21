package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.FullImageFragment.Companion.textArg
import ru.netology.nmedia.adapter.DateDecoration
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostLoadingStateAdapter
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.view.AuthViewModel
import ru.netology.nmedia.viewmodel.PostViewModel
import javax.inject.Inject

@AndroidEntryPoint
class FeedFragment : Fragment() {

    @Inject
    lateinit var appAuth: AppAuth

    @OptIn(ExperimentalCoroutinesApi::class)
    private val viewModel: PostViewModel by activityViewModels()
    val authViewModel: AuthViewModel by viewModels()

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)

        val adapter = PostsAdapter(object : OnInteractionListener {
            override fun onEdit(post: Post) {
                viewModel.edit(post)
            }

            override fun onLike(post: Post) {
                if (authViewModel.authorized) {
                    viewModel.likeById(post.id)
                } else {
                    context?.let { it1 ->
                        MaterialAlertDialogBuilder(
                            it1,
                            R.style.ThemeOverlay_MaterialComponents_Dialog_Alert
                        )
                            .setMessage(resources.getString(R.string.alert_dialog))
                            .setNeutralButton(resources.getString(R.string.sign_in_button)) { _, _ ->
                                findNavController().navigate(R.id.action_feedFragment_to_singInFragment)
                            }
                            .show()
                    }
                }
            }

            override fun onDeleteLike(post: Post) {
                viewModel.unlikeById(post.id)
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }

                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
            }

            override fun onFullImage(post: Post) {
                findNavController().navigate(R.id.action_feedFragment_to_fullImageFragment,
                    Bundle().apply {
                        textArg = post.attachment?.url
                    })
            }
        })

        binding.list.adapter = adapter.withLoadStateHeaderAndFooter(
            header = PostLoadingStateAdapter { adapter.retry() },
            footer = PostLoadingStateAdapter { adapter.retry() },
        )

   //     binding.list.addItemDecoration(DateDecoration())

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            binding.swiperefresh.isRefreshing = state.refreshing
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) { viewModel.loadPosts() }
                    .show()
            }
            if (state.removeError) {
                Snackbar.make(binding.root, R.string.error_remove, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) { viewModel.removeById(id.toLong()) }
                    .show()
            }
            if (state.likeError) {
                Snackbar.make(binding.root, R.string.error_like, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) {
                        viewModel.likeById(id.toLong())
                        viewModel.unlikeById(id.toLong())
                    }
                    .show()
            }
            if (state.saveError) {
                Snackbar.make(binding.root, R.string.error_save, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) { viewModel.save() }
                    .show()
            }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.data.collectLatest(adapter::submitData)
        }

        binding.swiperefresh.setOnRefreshListener(adapter::refresh)

        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.collectLatest { state ->
                binding.swiperefresh.isRefreshing =
                    state.refresh is LoadState.Loading
//                            state.prepend is LoadState.Loading ||
//                            state.append is LoadState.Loading
            }
        }

//        viewModel.newerCount.observe(viewLifecycleOwner) {
//            if (it > 0) {
//                binding.updateFab.isVisible = true
//            }
//        }

//        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
//            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
//                if (positionStart == 0) {
//                    binding.list.smoothScrollToPosition(0)
//                }
//            }
//        })

        binding.updateFab.setOnClickListener {
            viewModel.update()
            binding.updateFab.isVisible = false
        }

        var menuProvider: MenuProvider? = null

        authViewModel.data.observe(viewLifecycleOwner) {

            //adapter.refresh()

            binding.fab.setOnClickListener {
                if (authViewModel.authorized) {
                    findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
                } else {
                    context?.let { it1 ->
                        MaterialAlertDialogBuilder(
                            it1,
                            R.style.ThemeOverlay_MaterialComponents_Dialog_Alert
                        )
                            .setMessage(resources.getString(R.string.alert_dialog))
                            .setNeutralButton(resources.getString(R.string.sign_in_button)) { _, _ ->
                                findNavController().navigate(R.id.action_feedFragment_to_singInFragment)
                            }
                            .show()
                    }
                }
            }

            menuProvider?.let(requireActivity()::removeMenuProvider)

            requireActivity().addMenuProvider(object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.menu_auth, menu)

                    menu.setGroupVisible(R.id.authorized, authViewModel.authorized)
                    menu.setGroupVisible(R.id.unauthorized, !authViewModel.authorized)

                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                    when (menuItem.itemId) {
                        R.id.logout -> {
                            appAuth.removeAuth()
                            true
                        }
                        R.id.signIn -> {
                            findNavController().navigate(R.id.action_feedFragment_to_singInFragment)
                            true
                        }
                        R.id.signUp -> {
                            findNavController().navigate(R.id.action_feedFragment_to_signUpFragment)
                            true
                        }
                        else -> false
                    }
            }.apply {
                menuProvider = this
            }, viewLifecycleOwner)
        }


        return binding.root
    }
}
