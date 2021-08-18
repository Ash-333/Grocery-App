package com.ashish.grocery.notification

import com.ashish.grocery.Constants.Companion.CONTENT_TYPE
import com.ashish.grocery.Constants.Companion.SERVER_KEY
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface NotificationApi {

    //@Header("Authorization: key=$SERVER_KEY","Content-Type:$CONTENT_TYPE")


    @POST("fcm/send")
    suspend fun pushNotification(
        @Body notification : PushNotification
    ):retrofit2.Response<ResponseBody>


}