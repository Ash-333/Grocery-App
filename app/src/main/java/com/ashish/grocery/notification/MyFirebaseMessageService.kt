package com.ashish.grocery.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.ashish.grocery.MainShopActivity
import com.ashish.grocery.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class MyFirebaseMessageService : FirebaseMessagingService() {
    private val channelId="shop_channel"
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val intent=Intent(this,MainShopActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        val manager=getSystemService(Context.NOTIFICATION_SERVICE)
        createNotification(manager as NotificationManager)
        val intent1=PendingIntent.getActivities(this,0,
            arrayOf(intent),PendingIntent.FLAG_IMMUTABLE)

        val notification=NotificationCompat.Builder(this,channelId)
            .setContentTitle(message.data["title"])
            .setContentText(message.data["message"])
            .setSmallIcon(R.drawable.ic_delivery)
            .setAutoCancel(true)
            .setContentIntent(intent1)
            .build()

        manager.notify(Random.nextInt(),notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotification(manager:NotificationManager){
        val channel=NotificationChannel(channelId,"CartUpdate",NotificationManager.IMPORTANCE_HIGH)

        channel.description="Order status changed"
        channel.enableLights(true)
        manager.createNotificationChannel(channel)
    }
}