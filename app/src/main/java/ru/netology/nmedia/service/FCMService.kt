package ru.netology.nmedia.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class FCMService : FirebaseMessagingService() {
    private val action = "action"
    private val content = "content"
    private val channelId = "remote"
    private val gson = Gson()

    @Inject
    lateinit var appAuth: AppAuth

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_remote_name)
            val descriptionText = getString(R.string.channel_remote_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val userId = appAuth.state.value?.id
        val cont = gson.fromJson(message.data[content], MSG::class.java)

        handleId(userId, cont)

        message.data[action]?.let {
            val conte = gson.fromJson(message.data[content], Like::class.java)
            when (Action.valueOf(it)) {
                Action.LIKE -> handleLike(conte)
            }
        }
    }

    override fun onNewToken(token: String) {
        appAuth.sendPushToken(token)
    }

    private fun handleId(userId: Long?, date: MSG?) {
        val notification =
            NotificationCompat.Builder(this, channelId).setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(
                    date?.content
                ).setPriority(NotificationCompat.PRIORITY_DEFAULT).build()

        when {
            userId == date?.recipientId || date?.recipientId == null -> NotificationManagerCompat.from(
                this
            ).notify(Random.nextInt(100_000), notification)

            date?.recipientId == 0L || date?.recipientId != 0L || date?.recipientId != userId -> appAuth.sendPushToken()
        }
    }

    private fun handleLike(content: Like) {
        val notification =
            NotificationCompat.Builder(this, channelId).setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(
                    getString(
                        R.string.notification_user_liked,
                        content.userName,
                        content.postAuthor,
                    )
                ).setPriority(NotificationCompat.PRIORITY_DEFAULT).build()

        NotificationManagerCompat.from(this).notify(Random.nextInt(100_000), notification)
    }

}

enum class Action {
    LIKE,

}

data class Like(
    val userId: Long,
    val userName: String,
    val postId: Long,
    val postAuthor: String,
)

data class MSG(val recipientId: Long?, val content: String)
