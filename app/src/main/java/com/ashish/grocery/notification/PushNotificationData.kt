package com.ashish.grocery.notification

data class PushNotificationData(
    val data:NotificationData,
    val to:String?=""
)
