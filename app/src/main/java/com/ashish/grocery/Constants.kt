package com.ashish.grocery

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast

class Constants {

    val FCM_KEY="AAAAEP5drBM:APA91bE9bAVoPXhzPiVvOGnLXfP2q5cp0toEhf6_xrDxUdYnQfDEcLCnrQXmjTfFtZp48evEttEILsESCdxsteZfOp3TvoQfvHchnqxY14I7ubMwJMP1oXGxOT9-TpWX44N75E4z0eB8"

     fun showSuccessToast(message: String?,context: Context) {
        val toast = Toast(context)
        val view: View = LayoutInflater.from(context)
            .inflate(R.layout.success_toast, null)
        val tvMessage: TextView = view.findViewById(R.id.toastText)
        tvMessage.text = message
        toast.view = view
        toast.show()
    }

    companion object{
        const val BASE_URL="https://fcm.googleapis.com"
        const val SERVER_KEY="AAAAEP5drBM:APA91bE9bAVoPXhzPiVvOGnLXfP2q5cp0toEhf6_xrDxUdYnQfDEcLCnrQXmjTfFtZp48evEttEILsESCdxsteZfOp3TvoQfvHchnqxY14I7ubMwJMP1oXGxOT9-TpWX44N75E4z0eB8"
        const val CONTENT_TYPE="application/json"
        const val FCM_TOPIC="PUSH_NOTIFICATION"
    }
}