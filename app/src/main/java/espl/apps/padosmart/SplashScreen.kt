package espl.apps.padosmart

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import espl.apps.padosmart.bases.DeliveryUserBase
import espl.apps.padosmart.bases.EndUserBase
import espl.apps.padosmart.bases.ShopUserBase
import espl.apps.padosmart.repository.AuthRepository

class SplashScreen : AppCompatActivity() {

    private val TAG = "SplashScreenPlicly"
    private val authRepository = AuthRepository()

    var intentLogin: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val currentUser = authRepository.getFirebaseUser()

        if (currentUser != null) {
            var intent: Intent? = null
            val authResponse =
                authRepository.getFirebaseUserType(object : AuthRepository.AuthDataInterface {
                    override fun onAuthCallback(response: Int) {
                        Log.d(TAG, "User data fetched")
                        when (response) {
                            authRepository.END_USER -> {
                                Log.d(TAG, "User type: END_USER")
                                intent = Intent(applicationContext, EndUserBase::class.java)
                            }
                            authRepository.DELIVERY_USER -> {
                                Log.d(TAG, "User type: DELIVERY_USER")
                                intent = Intent(applicationContext, DeliveryUserBase::class.java)
                            }
                            authRepository.SHOP_USER -> {
                                Log.d(TAG, "User type: SHOP_USER")
                                intent = Intent(applicationContext, ShopUserBase::class.java)
                            }
                        }

                    }

                })
            if (authResponse == authRepository.AUTH_ACCESS_SUCCESSFUL) {
                startActivity(intent)
                finish()
            } else {
                Snackbar.make(
                    findViewById(R.id.content), "Unable to sign you in",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        } else {

//        // Manually subscribing to topic as some users do not get subscribed automatically.
//        FirebaseMessaging.getInstance().subscribeToTopic("Auth_User")
//            .addOnCompleteListener(OnCompleteListener<Void?> { task ->
//                var msg = "Successfully subscribed"
//                if (!task.isSuccessful) {
//                    msg = "Subscription failed"
//                }
//                Log.d(TAG, msg)
//            })

            Firebase.database.setPersistenceEnabled(true)
            intentLogin = Intent(applicationContext, Login::class.java)
            val countDownTimer: CountDownTimer = object : CountDownTimer(1000, 1000) {
                override fun onTick(millisecondsUntilDone: Long) {

                    //countdown is counting(every second)
                }

                override fun onFinish() {
                    Log.d(TAG, "Cached sign-in not found, asking for fresh sign-in..")
                    startActivity(intentLogin)
                    finish()

                }
            }
            countDownTimer.start()
        }
    }

//    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
//        Log.d(
//            TAG,
//            "firebaseAuthWithGoogle(SplashScreen.java) launched with : " + acct.id
//        )
//        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
//        mAuth.signInWithCredential(credential)
//            .addOnCompleteListener(
//                this
//            ) { task ->
//                Log.d(
//                    TAG,
//                    "signInWithCredential:onComplete:" + task.isSuccessful
//                )
//                //dialog.dismiss();
//                if (task.isSuccessful) {
//                    val `in` = Intent(this@SplashScreen, Base::class.java)
//                    startActivity(`in`)
//                    finish()
//                } else if (!task.isSuccessful) {
//                    Log.w(TAG, "signInWithCredential", task.exception)
//                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
//                    startActivity(intentLogin)
//                }
//            }
//    }
}