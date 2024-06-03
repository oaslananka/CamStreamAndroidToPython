package dev.oaslananka.cameraxrtspstreaming


import android.app.Activity
import android.content.pm.ActivityInfo
import com.pedro.encoder.input.video.CameraHelper

object ScreenOrientation {
    fun lockScreen(activity: Activity) {
        val currentOrientation = when (CameraHelper.getCameraOrientation(activity)) {
            90 -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            180 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            270 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
            else -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        activity.requestedOrientation = currentOrientation
    }

    fun unlockScreen(activity: Activity) {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }
}