package espl.apps.padosmart.bases

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import espl.apps.padosmart.R

class EndUserBase : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("hi", "in end user base")
    }
}