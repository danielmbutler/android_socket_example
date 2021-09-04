package com.dbtechprojects.socketexample

import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class SocketListener(private val listener: ChatSocketListener) : WebSocketListener() {
    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        listener.onMessage(webSocket, text)
        
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        listener.onOpen(webSocket, response)
        
    }
}

