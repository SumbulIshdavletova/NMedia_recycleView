package ru.netology.nmedia.dto

import android.media.Image
import android.net.Uri
import java.io.File
import java.sql.Timestamp
import java.text.DateFormat
import java.time.Instant
import java.time.ZonedDateTime

sealed interface FeedItem {
    val id: Long
}

data class Post(
    override val id: Long,
    val author: String,
    val authorAvatar: String,
    val content: String,
    val published: Long,
    val likedByMe: Boolean,
    val likes: Int = 0,
    val show: Boolean = true,
    val attachment: Attachment? = null,
    val authorId: Long = 0,
    val ownedByMe: Boolean = false,
) : FeedItem

data class Ad(
    override val id: Long,
    val image: String,
) : FeedItem

data class TimingSeparator(
    override var id: Long,
    val time: String,
) : FeedItem


data class PhotoModel(
    val uri: Uri?,
    val file: File?
)

data class Media(val id: String)

data class Attachment(
    val url: String,
    val type: AttachmentType
)

enum class AttachmentType {
    IMAGE
}

