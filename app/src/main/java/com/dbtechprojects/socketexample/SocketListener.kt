package com.dbtechprojects.socketexample

import android.util.Log
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class SocketListener(private val listener: ChatSocketListener) : WebSocketListener() {
    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        Log.d("SocketListener" , text.toString())
        listener.onMessage(webSocket, text)
        
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        Log.d("SocketListener" , response.toString())
        listener.onOpen(webSocket, response)
        
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        super.onFailure(webSocket, t, response)
        Log.d("SocketListener" , response.toString())
        t.printStackTrace()
    }
}

