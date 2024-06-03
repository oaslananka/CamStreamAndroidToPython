package dev.oaslananka.cameraxrtspstreaming

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import java.io.File

object PathUtils {
    @JvmStatic
    fun getRecordPath(): File {
        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
        return File(storageDir.absolutePath + "/RootEncoder")
    }

    @JvmStatic
    fun updateGallery(context: Context, path: String) {
        context.sendBroadcast(
            Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.fromFile(File(path))
            )
        )
    }
}