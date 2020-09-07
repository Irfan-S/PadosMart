package espl.apps.padosmart

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.hbb20.CountryCodePicker
import espl.apps.padosmart.bases.EndUserBase
import espl.apps.padosmart.bases.ShopBase
import espl.apps.padosmart.models.UserDataModel
import espl.apps.padosmart.repository.AuthRepository
import kotlinx.android.synthetic.main.activity_login.*
import java.util.concurrent.TimeUnit


class Login : AppCompatActivity(), View.OnClickListener {

    //Core data repo
    lateinit var authRepository: AuthRepository

    // [START declare_auth]
    lateinit var mAuth: FirebaseAuth

    // Declaring all views
    lateinit var buttonStartVerification: Button
    lateinit var buttonVerifyPhone: Button
    lateinit var buttonResend: Button
    lateinit var fieldPhoneNumber: EditText
    lateinit var fieldVerificationCode: EditText
    lateinit var shopSignupButton: Button

    lateinit var countryCodePicker: CountryCodePicker

    private var phoneNumber: String? = null

    private var mVerificationInProgress = false
    private var mVerificationId: String? = null
    private var mResendToken: ForceResendingToken? = null
    private var mCallbacks: OnVerificationStateChangedCallbacks? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        authRepository = AuthRepository(applicationContext)
        mAuth = authRepository.getFirebaseAuthReference()
        // Restore instance state
        savedInstanceState?.let { onRestoreInstanceState(it) }

        // Assign views
        buttonStartVerification = findViewById<Button>(R.id.buttonStartVerification)
        buttonVerifyPhone = findViewById<Button>(R.id.buttonVerifyPhone)
        buttonResend = findViewById<Button>(R.id.buttonResend)
        shopSignupButton = findViewById(R.id.buttonShopSignUp)

        //Assign click listeners
        shopSignupButton.setOnClickListener(this)
        buttonStartVerification.setOnClickListener(this)
        buttonVerifyPhone.setOnClickListener(this)
        buttonResend.setOnClickListener(this)

        //Assign field entries
        fieldPhoneNumber = findViewById(R.id.fieldPhoneNumber)
        fieldVerificationCode = findViewById(R.id.fieldVerificationCode)

        countryCodePicker = findViewById(R.id.ccp)
        countryCodePicker.registerCarrierNumberEditText(fieldPhoneNumber)

        phoneAuthFields.visibility = View.INVISIBLE


        // Initialize phone auth callbacks
        // [START phone_auth_callbacks]
        mCallbacks = object : OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:$credential")
                // [START_EXCLUDE silent]
                mVerificationInProgress = false
                // [END_EXCLUDE]

                // [START_EXCLUDE silent]
                // Update the UI and attempt sign in with the phone credential
                updateUI(STATE_VERIFY_SUCCESS)
                // [END_EXCLUDE]
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e)
                // [START_EXCLUDE silent]
                mVerificationInProgress = false
                // [END_EXCLUDE]
                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // [START_EXCLUDE]
                    fieldPhoneNumber.error = "Invalid phone number."
                    // [END_EXCLUDE]
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // [START_EXCLUDE]
                    Snackbar.make(
                        findViewById(android.R.id.content), "Quota exceeded.",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    // [END_EXCLUDE]
                }

                // Show a message and update the UI
                // [START_EXCLUDE]
                updateUI(STATE_VERIFY_FAILED)
                // [END_EXCLUDE]
            }

            override fun onCodeSent(
                verificationId: String,
                token: ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:$verificationId")

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId
                mResendToken = token

                // [START_EXCLUDE]
                // Update UI
                updateUI(STATE_CODE_SENT)
                // [END_EXCLUDE]
            }
        }
        // [END phone_auth_callbacks]

    }


//    // [START on_start_check_user]
//    public override fun onStart() {
//        super.onStart()
//        // Check if user is signed in (non-null) and update UI accordingly.
//        val currentUser = mAuth.currentUser
//        updateUI(currentUser)
//
//        // [START_EXCLUDE]
//        if (mVerificationInProgress && validatePhoneNumber()) {
//            startPhoneNumberVerification(fieldPhoneNumber.text.toString())
//        }
//        // [END_EXCLUDE]
//    }

    // [END on_start_check_user]
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS)
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,  // Phone number to verify
            60,  // Timeout duration
            TimeUnit.SECONDS,  // Unit of timeout
            this,  // Activity (for callback binding)
            mCallbacks!!
        ) // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]
        mVerificationInProgress = true
    }

    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        // [START verify_with_code]
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        // [END verify_with_code]

        signInWithPhoneAuthCredential(credential)
    }

    // [START resend_verification]
    private fun resendVerificationCode(
        phoneNumber: String,
        token: ForceResendingToken?
    ) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,  // Phone number to verify
            60,  // Timeout duration
            TimeUnit.SECONDS,  // Unit of timeout
            this,  // Activity (for callback binding)
            mCallbacks!!,  // OnVerificationStateChangedCallbacks
            token
        ) // ForceResendingToken from callbacks
    }

    // [END resend_verification]
    // [START sign_in_with_phone]
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(
                this
            ) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")

                    authRepository.setFirebaseUser(task.result!!.user!!)
                    authRepository.getFirebaseUserType(object : AuthRepository.AuthDataInterface {
                        override fun onAuthCallback(response: Long) {
                            Log.d(TAG, "User data fetched with response: $response")
                            if (task.result!!.user!!.isEmailVerified) {
                                Log.d(TAG, "Email verified user logging in")
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
                                            findViewById(android.R.id.content),
                                            "Unable to sign you in",
                                            Snackbar.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            } else {
                                Log.d(TAG, "Email unverified user")
                                when (response) {
                                    authRepository.END_USER.toLong() -> {
                                        Log.d(TAG, "User type: END_USER unverified")
                                        Snackbar.make(
                                            findViewById(android.R.id.content),
                                            "Please verify your email and try again",
                                            Snackbar.LENGTH_LONG
                                        ).show()
                                    }
                                    authRepository.NEW_USER.toLong() -> {
                                        Log.d(TAG, "User type: NEW_USER")
                                        intent =
                                            Intent(applicationContext, UserSignupActivity::class.java)
                                        val userData = UserDataModel(phone = phoneNumber)
                                        intent.putExtra(
                                            getString(R.string.userDataParcel),
                                            userData
                                        )
                                        startActivity(intent)
                                        finish()
                                    }
                                }
                            }
//                            when (response) {
//
//                                authRepository.NEW_USER.toLong() -> {
//                                    Log.d(TAG, "User type: NEW_USER")
//                                    intent = Intent(applicationContext, SignupActivity::class.java)
//                                    val userData = UserDataModel(phone = phoneNumber)
//                                    intent.putExtra(getString(R.string.userDataParcel), userData)
//                                    startActivity(intent)
//                                    authRepository.createEndUserAuthObject()
//                                    finish()
//                                }
//                                authRepository.AUTH_ACCESS_FAILED.toLong() -> {
//                                    Snackbar.make(
//                                        findViewById(R.id.content), "Unable to sign you in",
//                                        Snackbar.LENGTH_LONG
//                                    ).show()
//                                }
//                            }

                        }

                    })

                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        // [START_EXCLUDE silent]
                        fieldVerificationCode.error = "Invalid code."
                        // [END_EXCLUDE]
                    }
                    // [START_EXCLUDE silent]
                    // Update UI
                    updateUI(STATE_SIGNIN_FAILED)
                    // [END_EXCLUDE]
                }
            }
    }

    // [END sign_in_with_phone]
    private fun signOut() {
        mAuth.signOut()
    }


    private fun updateUI(
        uiState: Int
    ) {
        when (uiState) {
            STATE_CODE_SENT -> {
                // Code sent state, show the verification field, the
                phoneAuthFields.visibility = View.VISIBLE
                enableViews(
                    buttonVerifyPhone,
                    buttonResend,
                    fieldPhoneNumber,
                    fieldVerificationCode
                )
                disableViews(buttonStartVerification)
            }
            STATE_VERIFY_FAILED -> {
                // Verification has failed, show all options
                enableViews(
                    buttonStartVerification,
                    buttonVerifyPhone,
                    buttonResend,
                    fieldPhoneNumber,
                    fieldVerificationCode
                )
            }
            STATE_SIGNIN_FAILED ->                 // No-op, handled by sign-in check
            {

            }
        }
    }

    private fun validatePhoneNumber(): Boolean {
        phoneNumber = countryCodePicker.fullNumberWithPlus
        Log.d(TAG, "Number :$phoneNumber")
        if (TextUtils.isEmpty(phoneNumber)) {
            fieldPhoneNumber.error = "Invalid phone number."
            return false
        }
        return true
    }

    private fun enableViews(vararg views: View) {
        for (v in views) {
            v.isEnabled = true
        }
    }

    private fun disableViews(vararg views: View) {
        for (v in views) {
            v.isEnabled = false
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.buttonStartVerification -> {
                if (!validatePhoneNumber()) {
                    return
                }
                startPhoneNumberVerification(phoneNumber!!)
            }
            R.id.buttonVerifyPhone -> {
                val code = fieldVerificationCode.text.toString()
                if (TextUtils.isEmpty(code)) {
                    fieldVerificationCode.error = "Cannot be empty."
                    return
                }
                verifyPhoneNumberWithCode(mVerificationId, code)
            }
            R.id.buttonResend -> {
                validatePhoneNumber()
                resendVerificationCode(
                    phoneNumber!!, mResendToken
                )
            }
            R.id.buttonShopSignUp -> {

            }
        }
    }

    companion object {
        private const val TAG = "LoginActivity"
        private const val KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress"
        private const val STATE_CODE_SENT = 2
        private const val STATE_VERIFY_FAILED = 3
        private const val STATE_VERIFY_SUCCESS = 4
        private const val STATE_SIGNIN_FAILED = 5
    }
}