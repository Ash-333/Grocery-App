package com.ashish.grocery.notification.api

import com.ashish.grocery.notification.PushNotificationData
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiInterface {
    @Headers("Content-Type:application/json","Authorization:key=AAAAEP5drBM:APA91bE9bAVoPXhzPiVvOGnLXfP2q5cp0toEhf6_xrDxUdYnQfDEcLCnrQXmjTfFtZp48evEttEILsESCdxsteZfOp3TvoQfvHchnqxY14I7ubMwJMP1oXGxOT9-TpWX44N75E4z0eB8")
    @POST("fcm/send")
    fun sendNotification(@Body notification: PushNotificationData): Call<PushNotificationData>
}