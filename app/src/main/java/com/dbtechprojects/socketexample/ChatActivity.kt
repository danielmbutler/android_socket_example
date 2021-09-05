package com.dbtechprojects.socketexample

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dbtechprojects.socketexample.databinding.ActivityChatBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class ChatActivity : AppCompatActivity(), ChatSocketListener, TextWatcher {

    private lateinit var name: String
    private lateinit var webSocket: WebSocket
    private lateinit var chatMessageAdapter: ChatMessageAdapter
    private var SERVER_PATH = "ws://192.168.1.134:3000"
    private var _binding: ActivityChatBinding? = null
    private val binding: ActivityChatBinding get() = _binding!!
    val imagePickerActivity = registerForActivityResult(ActivityResultContracts.GetContent()) {
        val inputStream = contentResolver.openInputStream(it)
        val image = BitmapFactory.decodeStream(inputStream)
        image?.let { imageToSend ->
            sendImage(imageToSend)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        name = intent.getStringExtra("name").toString()
        initiateSockConnection()
    }

    private fun initiateSockConnection() {
        val client = OkHttpClient()
        val request = Request.Builder().url(SERVER_PATH).build()
        webSocket = client.newWebSocket(request, SocketListener(this))
    }

    private fun initializeView() {
        binding.messageText.addTextChangedListener(this)
        binding.sendButton.setOnClickListener {
            sendMessage()
        }
        binding.pickImageButton.setOnClickListener {
            pickImage()
        }
        chatMessageAdapter = ChatMessageAdapter(layoutInflater, mutableListOf())
        binding.chatRecyclerview.apply {
            this.adapter = chatMessageAdapter
            this.layoutManager = LinearLayoutManager(this@ChatActivity)
        }
    }

    private fun pickImage() {
        Log.d("CHAT", "picking image")

        imagePickerActivity.launch("image/*")
    }

    private fun sendImage(image: Bitmap) {
        val byteArrayOutputStream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        val imageAsString =
            Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT)
        val jsonObject = JSONObject()

        try {
            jsonObject.put("name", name)
            jsonObject.put("image", imageAsString)
            webSocket.send(jsonObject.toString())
            jsonObject.put("isSent", true)
            chatMessageAdapter.addItem(jsonObject)
            binding.chatRecyclerview.smoothScrollToPosition(chatMessageAdapter.itemCount - 1)
        } catch (e: JSONException) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendMessage() {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("name", name)
            jsonObject.put("message", binding.messageText.text.toString())
            webSocket.send(jsonObject.toString())
            jsonObject.put("isSent", true)
            resetMessageEditText()
            chatMessageAdapter.addItem(jsonObject)
            binding.chatRecyclerview.smoothScrollToPosition(chatMessageAdapter.itemCount - 1)
        } catch (e: JSONException) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()

        }
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@ChatActivity, "Connection Successful",
                    Toast.LENGTH_SHORT
                ).show()

                initializeView()
            }
        }
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                try {
                    val jsonObject = JSONObject(text)
                    jsonObject.put("isSent", false)
                    chatMessageAdapter.addItem(jsonObject)
                    binding.chatRecyclerview.smoothScrollToPosition(chatMessageAdapter.itemCount - 1)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

    }

    override fun afterTextChanged(s: Editable?) {
        Log.d("textListener", s.toString())
        //remove all whitespace
        val string = s.toString().trim()
        if (string.isEmpty()) {
            resetMessageEditText()
        } else {
            binding.sendButton.visibility = View.VISIBLE
            binding.pickImageButton.visibility = View.INVISIBLE
        }
    }

    private fun resetMessageEditText() {
        binding.messageText.removeTextChangedListener(this)
        binding.messageText.setText("")
        binding.sendButton.visibility = View.INVISIBLE
        binding.pickImageButton.visibility = View.VISIBLE
        binding.messageText.addTextChangedListener(this)
    }

}