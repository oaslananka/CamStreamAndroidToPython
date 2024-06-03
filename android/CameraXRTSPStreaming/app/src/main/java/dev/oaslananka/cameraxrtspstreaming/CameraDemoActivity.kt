package dev.oaslananka.cameraxrtspstreaming


import android.graphics.SurfaceTexture
import android.os.Bundle
import android.view.TextureView
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pedro.common.ConnectChecker
import com.pedro.encoder.input.video.CameraHelper
import com.pedro.encoder.input.video.CameraOpenException
import com.pedro.library.base.recording.RecordController
import com.pedro.library.view.AutoFitTextureView
import com.pedro.rtspserver.RtspServerCamera1
import com.pedro.rtspserver.server.ClientListener
import com.pedro.rtspserver.server.ServerClient
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CameraDemoActivity : AppCompatActivity(), ConnectChecker, ClientListener,
    TextureView.SurfaceTextureListener {

    private lateinit var rtspServerCamera1: RtspServerCamera1
    private lateinit var bStream: ImageView
    private lateinit var bRecord: ImageView
    private lateinit var bSwitchCamera: ImageView
    private lateinit var surfaceView: AutoFitTextureView
    private lateinit var tvUrl: TextView
    private var recordPath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_camera_demo)
        tvUrl = findViewById(R.id.rtsp_url_text_view)
        bStream = findViewById(R.id.start_stop_button)
        bRecord = findViewById(R.id.record_button)
        bSwitchCamera = findViewById(R.id.switch_camera_button)
        surfaceView = findViewById(R.id.surfaceView)
        rtspServerCamera1 = RtspServerCamera1(surfaceView, this, 1935)
        rtspServerCamera1.streamClient.setClientListener(this)
        surfaceView.surfaceTextureListener = this

        bStream.setOnClickListener {
            if (rtspServerCamera1.isStreaming) {
                bStream.setImageResource(R.drawable.stream_icon)
                rtspServerCamera1.stopStream()
                if (!rtspServerCamera1.isRecording) ScreenOrientation.unlockScreen(this)
            } else if (rtspServerCamera1.isRecording || prepare()) {
                bStream.setImageResource(R.drawable.stream_stop_icon)
                rtspServerCamera1.startStream()
                tvUrl.text = rtspServerCamera1.streamClient.getEndPointConnection()
                ScreenOrientation.lockScreen(this)
            } else {
                toast("Error preparing stream, This device cant do it")
            }
        }
        bRecord.setOnClickListener {
            if (rtspServerCamera1.isRecording) {
                rtspServerCamera1.stopRecord()
                bRecord.setImageResource(R.drawable.record_icon)
                PathUtils.updateGallery(this, recordPath)
                if (!rtspServerCamera1.isStreaming) ScreenOrientation.unlockScreen(this)
            } else if (rtspServerCamera1.isStreaming || prepare()) {
                val folder = PathUtils.getRecordPath()
                if (!folder.exists()) folder.mkdir()
                val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                recordPath = "${folder.absolutePath}/${sdf.format(Date())}.mp4"
                bRecord.setImageResource(R.drawable.pause_icon)
                rtspServerCamera1.startRecord(recordPath) { status ->
                    if (status == RecordController.Status.RECORDING) {
                        bRecord.setImageResource(R.drawable.stop_icon)
                    }
                }
                ScreenOrientation.lockScreen(this)
            } else {
                toast("Error preparing stream, This device cant do it")
            }
        }
        bSwitchCamera.setOnClickListener {
            try {
                rtspServerCamera1.switchCamera()
            } catch (e: CameraOpenException) {
                Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun prepare(): Boolean {
        val prepared = rtspServerCamera1.prepareAudio() && rtspServerCamera1.prepareVideo()
        adaptPreview()
        return prepared
    }

    private fun adaptPreview() {
        val isPortrait = CameraHelper.isPortrait(this)
        val w = if (isPortrait) rtspServerCamera1.streamHeight else rtspServerCamera1.streamWidth
        val h = if (isPortrait) rtspServerCamera1.streamWidth else rtspServerCamera1.streamHeight
        surfaceView.setAspectRatio(w, h)
    }

    override fun onNewBitrate(bitrate: Long) {

    }

    override fun onConnectionSuccess() {
        toast("Connected")
    }

    override fun onConnectionFailed(reason: String) {
        toast("Failed: $reason")
        rtspServerCamera1.stopStream()
        if (!rtspServerCamera1.isRecording) ScreenOrientation.unlockScreen(this)
        bStream.setImageResource(R.drawable.stream_icon)
    }

    override fun onConnectionStarted(url: String) {
    }

    override fun onDisconnect() {
        toast("Disconnected")
    }

    override fun onAuthError() {
        toast("Auth error")
        rtspServerCamera1.stopStream()
        if (!rtspServerCamera1.isRecording) ScreenOrientation.unlockScreen(this)
        bStream.setImageResource(R.drawable.stream_icon)
    }

    override fun onAuthSuccess() {
        toast("Auth success")
    }

    override fun onClientConnected(client: ServerClient) {
        toast("Client connected: ${client.clientAddress}")
    }

    override fun onClientDisconnected(client: ServerClient) {
        toast("Client disconnected: ${client.clientAddress}")
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        if (!rtspServerCamera1.isOnPreview) {
            rtspServerCamera1.startPreview()
            adaptPreview()
        }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {

    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        if (rtspServerCamera1.isRecording) {
            rtspServerCamera1.stopRecord()
            bRecord.setBackgroundResource(R.drawable.record_icon)
            PathUtils.updateGallery(this, recordPath)
        }
        if (rtspServerCamera1.isStreaming) {
            rtspServerCamera1.stopStream()
            bStream.setImageResource(R.drawable.stream_icon)
        }
        if (rtspServerCamera1.isOnPreview) rtspServerCamera1.stopPreview()
        ScreenOrientation.unlockScreen(this)
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {

    }
}