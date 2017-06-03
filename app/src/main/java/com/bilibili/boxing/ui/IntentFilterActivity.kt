package com.bilibili.boxing.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.bilibili.boxing.demo.R
import com.bilibili.boxing.model.BoxingManager
import com.bilibili.boxing.model.config.BoxingConfig
import com.bilibili.boxing.model.config.BoxingCropOption
import com.bilibili.boxing.model.entity.BaseMedia
import com.bilibili.boxing.utils.BoxingFileHelper
import com.bilibili.boxing_impl.ui.BoxingActivity
import java.util.*

/**
 * A demo to show a Picker Activity with intent filter, which can start by other apps.
 * Get absolute path through [Intent.getDataString] in [.onActivityResult].

 * @author ChenSL
 */

class IntentFilterActivity : BoxingActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // in DCIM/bili/boxing
        val cropPath = BoxingFileHelper.boxingPathInDCIM
        if (TextUtils.isEmpty(cropPath)) {
            Toast.makeText(applicationContext, R.string.boxing_storage_deny, Toast.LENGTH_SHORT).show()
            return
        }
        val destUri = Uri.Builder()
                .scheme("file")
                .appendPath(cropPath)
                .appendPath(String.format(Locale.US, "%s.jpg", System.currentTimeMillis()))
                .build()
        val config = BoxingConfig(BoxingConfig.Mode.SINGLE_IMG).needCamera(R.drawable.ic_boxing_camera_white).withCropOption(BoxingCropOption(destUri))
        BoxingManager.instance.boxingConfig = config
        super.onCreate(savedInstanceState)

    }

    override fun onBoxingFinish(intent: Intent, medias: List<BaseMedia>?) {
        if (medias != null && medias.isNotEmpty()) {
            intent.data = Uri.parse(medias[0].path)
            setResult(Activity.RESULT_OK, intent)
        } else {
            setResult(Activity.RESULT_CANCELED, null)
        }
        finish()
    }
}


