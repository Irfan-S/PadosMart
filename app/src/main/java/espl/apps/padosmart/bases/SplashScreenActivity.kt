package espl.apps.padosmart.bases

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import espl.apps.padosmart.R
import espl.apps.padosmart.models.AccessDataModel
import espl.apps.padosmart.models.ShopDataModel
import espl.apps.padosmart.models.UserDataModel
import espl.apps.padosmart.repository.AuthRepository
import espl.apps.padosmart.utils.AUTH_ACCESS_FAILED
import espl.apps.padosmart.utils.END_USER
import espl.apps.padosmart.utils.SHOP_USER

class SplashScreenActivity : AppCompatActivity() {

    private val TAG = "SplashScreen"


    var intentLogin: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        Firebase.database.setPersistenceEnabled(true)
        val authRepository = AuthRepository(applicationContext)

        intentLogin = Intent(applicationContext, LoginBase::class.java)
        intentLogin!!.putExtra(getString(R.string.intent_userType), AUTH_ACCESS_FAILED)
        val currentUser = authRepository.getFirebaseUser()
        val countDownTimer: CountDownTimer = object : CountDownTimer(1000, 1000) {
            override fun onTick(millisecondsUntilDone: Long) {

                //countdown is counting(every second)
            }

            override fun onFinish() {
                Log.d(TAG, "Cached sign-in not found, asking for fresh sign-in..")
                authRepository.signOut()
                startActivity(intentLogin)
                finish()
            }
        }



        if (currentUser != null) {
            if (currentUser.isEmailVerified) {
                authRepository.getFirebaseUserType(object : AuthRepository.AuthDataInterface {
                    override fun onAuthCallback(accessDataModel: AccessDataModel) {
                        Log.d(TAG, "User data fetched with response: ${accessDataModel.accessCode}")
                        when (accessDataModel.accessCode) {
                            END_USER -> {
                                Log.d(TAG, "User type: END_USER")
                                intent =
                                    Intent(
                                        applicationContext,
                                        UserBase::class.java
                                    )
                                intent.putExtra(
                                    getString(R.string.intent_userType),
                                    END_USER
                                )
                                authRepository.getEndUserDataObject(callback = object :
                                    AuthRepository.UserDataInterface {
                                    override fun onUploadCallback(success: Boolean) {
                                        //Nothing
                                    }

                                    override fun onDataFetch(dataModel: UserDataModel) {
                                        intent.putExtra("userData", dataModel)
                                        startActivity(intent)
                                        finish()
                                    }

                                })
                            }
                            SHOP_USER -> {
                                Log.d(TAG, "User type: SHOP_USER")
                                intent = Intent(applicationContext, UserBase::class.java)
                                intent.putExtra(
                                    getString(R.string.intent_userType),
                                    SHOP_USER
                                )
                                authRepository.fetchShopDataObject(object :
                                    AuthRepository.ShopDataFetch {
                                    override fun onFetchComplete(shopDataModel: ShopDataModel?) {
                                        intent.putExtra("shopData", shopDataModel)
                                        Log.d(TAG, "Shop data: $shopDataModel")
                                        startActivity(intent)
                                        finish()
                                    }
                                })
                            }
                            AUTH_ACCESS_FAILED -> {
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