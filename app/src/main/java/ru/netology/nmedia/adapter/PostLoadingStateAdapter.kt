package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.databinding.LoadStateFooterHeaderViewItemBinding

class PostLoadingStateAdapter(
    private val retryListener: () -> Unit,
) : LoadStateAdapter<PostLoadingViewHolder>() {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): PostLoadingViewHolder {
        val binding = LoadStateFooterHeaderViewItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostLoadingViewHolder(binding, retryListener)
    }

    override fun onBindViewHolder(holder: PostLoadingViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

}




class PostLoadingViewHolder(
    private val footerLoadingBinding: LoadStateFooterHeaderViewItemBinding,
    private val retryListener: () -> Unit,
) : RecyclerView.ViewHolder(footerLoadingBinding.root) {

    fun bind(loadState: LoadState) {
        footerLoadingBinding.apply {
            footerLoadingBinding.progressBar.isVisible = loadState is LoadState.Loading
            footerLoadingBinding.retryButton.isVisible = loadState is LoadState.Error
            retryButton.setOnClickListener { retryListener() }
        }
    }
}

