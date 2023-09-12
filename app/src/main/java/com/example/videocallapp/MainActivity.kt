package com.example.videocallapp

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.videocallapp.databinding.ActivityMainBinding
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.video.VideoCanvas
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    private lateinit var binding :ActivityMainBinding
    private val appId = "f778c21328394e10994358a1889bdede"

    private val channelName = "rishiCoder"
    private val tokenName = "007eJxTYODImPpRdc6J7vlzHz3KzU5c8vzTyy2ym9WmFl2zCMl9XX1UgSHN3Nwi2cjQ2MjC2NIk1dDA0tLE2NQi0dDCwjIpJTUllX0eQ2pDICPDBaZXDIxQCOJzMRRlFmdkOuenpBYxMAAAxkAjYw=="
    private val uid =0
    private var isJoined = false
    private var agoraEngine : RtcEngine? = null
    private var localSurfaceView : SurfaceView? = null
    private var remoteSurfaceView : SurfaceView? = null

    private var PERMISSION_ID = 22
    private  val REQUESTED_PERMISSION =
        arrayOf(
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.CAMERA,
        )
    private fun checkSelfPermission(): Boolean{
        return !(ContextCompat.checkSelfPermission(this,REQUESTED_PERMISSION[0])!= PackageManager.PERMISSION_GRANTED
                ||ContextCompat.checkSelfPermission(this,REQUESTED_PERMISSION[1])!= PackageManager.PERMISSION_GRANTED)
    }


    private fun setUpVideoSdkEngine(){
        try {
            val config = RtcEngineConfig()
            config.mContext = baseContext
            config.mAppId = appId
            config.mEventHandler = mRtcEventHandler
            agoraEngine = RtcEngine.create(config)
            agoraEngine!!.enableVideo()
        }catch (e : Exception){
            Log.d("error",e.toString())
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (!checkSelfPermission()){
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSION,PERMISSION_ID)
        }
        setUpVideoSdkEngine()
        binding.joinButton.setOnClickListener {
            joinCall()
        }
        binding.leaveButton.setOnClickListener {
            leaveCall()
        }
    }
    private fun joinCall(){
        if (checkSelfPermission()){
            val option = ChannelMediaOptions()
            option.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
            option.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
            setUpLocalVideo()
            localSurfaceView?.visibility = VISIBLE
            agoraEngine!!.startPreview()
            agoraEngine!!.joinChannel(tokenName, channelName,uid,option)

        }else{
            Toast.makeText(this, "permission not granted", Toast.LENGTH_SHORT).show()
        }

    }
    private fun leaveCall(){
        if (!isJoined){
            Toast.makeText(this, "join please", Toast.LENGTH_SHORT).show()
        }else{
            agoraEngine!!.leaveChannel()
            if(remoteSurfaceView != null) remoteSurfaceView?.visibility = GONE
            if (remoteSurfaceView != null ) localSurfaceView?.visibility = GONE
            isJoined = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        agoraEngine!!.stopPreview()
        agoraEngine!!.leaveChannel()
        Thread{
            RtcEngine.destroy()
            agoraEngine = null
        }.start()
    }
    private val mRtcEventHandler : IRtcEngineEventHandler =
        object : IRtcEngineEventHandler(){

            override fun onUserJoined(uid: Int, elapsed: Int) {
                runOnUiThread {
                    setUpRemoteVideo(uid)
                    Toast.makeText(this@MainActivity, "user Joined ", Toast.LENGTH_SHORT).show()

                }
            }

            override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                isJoined = true
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "user Joined successfull $channel", Toast.LENGTH_SHORT).show()

                }

            }

            override fun onUserOffline(uid: Int, reason: Int) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "user leave ", Toast.LENGTH_SHORT).show()
                    remoteSurfaceView?.visibility  = GONE
                }

            }
        }
    private fun setUpRemoteVideo(uId : Int){
        remoteSurfaceView = SurfaceView(baseContext)
        remoteSurfaceView!!.setZOrderMediaOverlay(true)
        binding.remoteUser.addView(remoteSurfaceView)
        agoraEngine!!.setupRemoteVideo(
            VideoCanvas(remoteSurfaceView,VideoCanvas.RENDER_MODE_FIT,uId)
        )
    }
    private fun setUpLocalVideo(){
        localSurfaceView = SurfaceView(baseContext)
        binding.localUser.addView(localSurfaceView)
        agoraEngine!!.setupLocalVideo(
            VideoCanvas(localSurfaceView,VideoCanvas.RENDER_MODE_FIT,0)
        )
    }
}