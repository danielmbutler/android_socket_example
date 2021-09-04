package com.dbtechprojects.socketexample

import okhttp3.Response
import okhttp3.WebSocket

interface ChatSocketListener {
    fun onOpen(webSocket: WebSocket, response: Response)
    fun onMessage(webSocket: WebSocket, text: String)

}