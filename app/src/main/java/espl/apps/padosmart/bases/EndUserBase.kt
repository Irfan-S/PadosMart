package espl.apps.padosmart.bases

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import espl.apps.padosmart.Login
import espl.apps.padosmart.R

class EndUserBase : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("hi", "in end user base")

        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, Login::class.java))
            finish()
        }
    }
}