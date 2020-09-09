package espl.apps.padosmart

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import espl.apps.padosmart.bases.AuthBase
import espl.apps.padosmart.bases.EndUserBase
import espl.apps.padosmart.bases.ShopBase
import espl.apps.padosmart.repository.AuthRepository

class SplashScreenActivity : AppCompatActivity() {

    private val TAG = "SplashScreen"


    var intentLogin: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        Firebase.database.setPersistenceEnabled(true)
        val authRepository = AuthRepository(applicationContext)

        intentLogin = Intent(applicationContext, AuthBase::class.java)
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

        val currentUser = authRepository.getFirebaseUser()

        if (currentUser != null) {

            if (currentUser.isEmailVerified) {
                authRepository.getFirebaseUserType(object : AuthRepository.AuthDataInterface {
                    override fun onAuthCallback(response: Long) {
                        Log.d(TAG, "User data fetched with response: $response")
                        when (response) {
                            authRepository.END_USER.toLong() -> {
                                Log.d(TAG, "User type: END_USER")
                                intent = Intent(applicationContext, EndUserBase::class.java)
                                startActivity(intent)
                                finish()
                            }
                            authRepository.SHOP_USER.toLong() -> {
                                Log.d(TAG, "User type: SHOP_USER")
                                intent = Intent(applicationContext, ShopBase::class.java)
                                startActivity(intent)
                                finish()
                            }
                            authRepository.AUTH_ACCESS_FAILED.toLong() -> {
                                Snackbar.make(
                                    findViewById(android.R.id.content), "Unable to sign you in",
                                    Snackbar.LENGTH_LONG
                                ).show()
                            }
                            else -> {
                                countDownTimer.start()
                            }
                        }

                    }

                })
            } else {
                countDownTimer.start()
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