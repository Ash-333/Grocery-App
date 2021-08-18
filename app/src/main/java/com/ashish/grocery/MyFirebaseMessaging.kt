package com.ashish.grocery

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Build.VERSION_CODES
import androidx.annotation.RequiresApi
import com.ashish.grocery.seller.OrderDetailSellerActivity
import com.ashish.grocery.user.OrderDetailUserActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*
import 	android.graphics.BitmapFactory
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat

const val CHANNEL_ID="MY_NOTIFICATION_CHANNEL_ID"
class MyFirebaseMessaging:FirebaseMessagingService() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser
    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        firebaseAuth= FirebaseAuth.getInstance()
        firebaseUser=firebaseAuth.currentUser!!

        val notificationType: String = remoteMessage.data["notificationType"]!!
        if (notificationType == "New order"){
            val buyerUid= remoteMessage.data["buyerUid"]!!
            val orderId= remoteMessage.data["orderId"]!!
            val sellerUid= remoteMessage.data["sellerUid"]!!
            val notificationTitle= remoteMessage.data["notificationTitle"]!!
            val notificationMessage= remoteMessage.data["notificationMessage"]!!
            //val notificationDescription= remoteMessage.data["notificationDescription"]!!

            if (firebaseUser!=null && firebaseAuth.uid==sellerUid){
                showNotification(orderId,buyerUid,sellerUid,notificationTitle,notificationMessage,notificationType)
            }
        }
        if (notificationType == "Order Status Changed"){
            val buyerUid= remoteMessage.data["buyerUid"]!!
            val orderId= remoteMessage.data["orderId"]!!
            val sellerUid= remoteMessage.data["sellerUid"]!!
            val notificationTitle= remoteMessage.data["notificationTitle"]!!
            val notificationMessage= remoteMessage.data["notificationMessage"]!!
            //val notificationDescription= remoteMessage.data["notificationDescription"]!!

            if (firebaseUser!=null && firebaseAuth.uid==buyerUid){
                showNotification(orderId,buyerUid,sellerUid,notificationTitle,notificationMessage,notificationType)
            }
        }
    }

    private fun showNotification(orderId:String, buyerUid:String, sellerUid:String, notificationTitle:String, notificationMessage:String, notificationType:String){
        val notificationManager:NotificationManager= getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId=Random().nextInt(3000)
        if (Build.VERSION.SDK_INT>= VERSION_CODES.O){
            setupNotificationChannel(notificationManager)
        }
        //var intent:Intent
        lateinit var intent:Intent
        if (notificationType=="NewOrder"){
            intent=Intent(this,OrderDetailSellerActivity::class.java)
            intent.putExtra("orderId",orderId)
            intent.putExtra("orderBy",buyerUid)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        else if (notificationType=="OrderStatusChanged"){
            intent=Intent(this,OrderDetailUserActivity::class.java)
            intent.putExtra("orderId",orderId)
            intent.putExtra("orderTo",sellerUid)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pendingIntent=PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT)

        val largeIcon:Bitmap=BitmapFactory.decodeResource(resources,R.mipmap.ic_launcher)
        val notificationSound=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder=NotificationCompat.Builder(this,CHANNEL_ID)
        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(largeIcon)
            .setContentTitle(notificationTitle)
            .setContentText(notificationMessage)
            .setSound(notificationSound)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
        notificationManager.notify(notificationId,notificationBuilder.build())
    }

    @RequiresApi(VERSION_CODES.O)
    private fun setupNotificationChannel(notificationManager: NotificationManager) {
        val channelName="Some Sample Text"
        val channelDescription="Channel Description here"

        val notificationChannel=NotificationChannel(CHANNEL_ID,channelName,NotificationManager.IMPORTANCE_HIGH)
        notificationChannel.description = channelDescription
        notificationChannel.enableLights(true)
        notificationChannel.lightColor=Color.RED
        notificationChannel.enableVibration(true)
        if (notificationManager!=null){
            notificationManager.createNotificationChannel(notificationChannel)
        }

    }
}