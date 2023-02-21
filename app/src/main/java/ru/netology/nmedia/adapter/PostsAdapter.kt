package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardAdBinding
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.databinding.SeparatorViewTimestampBinding
import ru.netology.nmedia.dto.Ad

import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.TimingSeparator
import ru.netology.nmedia.view.load
import ru.netology.nmedia.view.loadCircleCrop
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

private val empty = Post(
    id = 0,
    content = "",
    author = "",
    authorAvatar = "",
    likedByMe = false,
    likes = 0,
    published = 0
)

interface OnInteractionListener {
    fun onLike(post: Post) {}
    fun onDeleteLike(post: Post)
    fun onEdit(post: Post) {}
    fun onRemove(post: Post) {}
    fun onShare(post: Post) {}
    fun onFullImage(post: Post) {}
}

class PostsAdapter(
    private val onInteractionListener: OnInteractionListener,
) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(PostDiffCallback()) {
    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is Ad -> R.layout.card_ad
            is Post -> R.layout.card_post
            is TimingSeparator -> R.layout.separator_view_timestamp
            null -> R.layout.card_post
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            R.layout.card_post -> {
                val binding =
                    CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return PostViewHolder(binding, onInteractionListener)
            }
            R.layout.card_ad -> {
                val binding =
                    CardAdBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return AdViewHolder(binding)
            }
            R.layout.separator_view_timestamp -> {
                val binding = SeparatorViewTimestampBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return TimeViewHolder(binding)
            }

            else -> error("unknown item type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {


        when (val item = getItem(position)) {
            is Ad -> (holder as? AdViewHolder)?.bind(item)

            is Post -> (holder as? PostViewHolder)?.bind(item)
            is TimingSeparator -> (holder as? TimeViewHolder)?.bind(item)
            null -> (holder as? PostViewHolder)?.bind(empty)
        }
    }
}

class AdViewHolder(
    private val binding: CardAdBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(ad: Ad) {
        binding.imageAd.load("${BuildConfig.BASE_URL}/media/${ad.image}")
    }
}

class TimeViewHolder(
    private val binding: SeparatorViewTimestampBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(time: TimingSeparator) {

        binding.separatorDescription.text = time.time

    }
}


class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(post: Post) {
        binding.apply {
            author.text = post.author

            val lEpochMilliSeconds: Long = post.published
            val zone = ZonedDateTime.ofInstant(
                Instant.ofEpochMilli(lEpochMilliSeconds), ZoneId.systemDefault()
            )
            published.text = post.published.toString()

            content.text = post.content
            avatar.loadCircleCrop("${BuildConfig.BASE_URL}/avatars/${post.authorAvatar}")
            like.isChecked = post.likedByMe
            like.text = "${post.likes}"

            if (post.attachment?.url != null) {
                attachment.isVisible = true
                val url = "${BuildConfig.BASE_URL}/media/${post.attachment.url}"
                Glide.with(attachment)
                    .load(url)
                    .placeholder(R.drawable.ic_baseline_rotate_right_24)
                    .error(R.drawable.ic_baseline_error_24)
                    .timeout(10_000)
                    .into(binding.attachment)
            }

            attachment.setOnClickListener {
                onInteractionListener.onFullImage(post)
            }

            menu.isVisible = post.ownedByMe
            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(post)
                                true
                            }
                            R.id.edit -> {
                                onInteractionListener.onEdit(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }

            if (post.likedByMe) {
                like.setOnClickListener {
                    onInteractionListener.onDeleteLike(post)
                }
            } else {
                like.setOnClickListener {
                    onInteractionListener.onLike(post)
                }
            }

            share.setOnClickListener {
                onInteractionListener.onShare(post)
            }
        }
    }
}


class PostDiffCallback : DiffUtil.ItemCallback<FeedItem>() {
    override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        if (oldItem::class != newItem::class) {
            return false
        }
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        return oldItem == newItem
    }

}