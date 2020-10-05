package espl.apps.padosmart.utils

import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import espl.apps.padosmart.models.AppVersionDataModel
import kotlin.random.Random


fun generateOTP(): Int {
    return Random.nextInt(999, 9999)
}

fun getLocalAppVersion(app: Application): AppVersionDataModel {
    return try {
        val pInfo: PackageInfo =
            app.packageManager.getPackageInfo(app.packageName, 0)
        val versionCode: String = pInfo.versionName
        AppVersionDataModel(versionCode)

    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        AppVersionDataModel()
    }
}