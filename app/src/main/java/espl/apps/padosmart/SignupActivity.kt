package espl.apps.padosmart

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import espl.apps.padosmart.models.UserDataModel
import espl.apps.padosmart.repository.AuthRepository


class SignupActivity : AppCompatActivity(), View.OnClickListener {

    val TAG = "signupActivity"

    lateinit var authRepository: AuthRepository

    //Declare editFields
    lateinit var userNameField: EditText
    lateinit var userPhoneTextView: TextView
    lateinit var userEmailField: EditText
    lateinit var userAddressField: EditText

    lateinit var userData: UserDataModel

    //Declare buttons
    lateinit var submitDetailsButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        authRepository = AuthRepository(applicationContext)

        userData = intent.getParcelableExtra(getString(R.string.userDataParcel)) as UserDataModel

        userNameField = findViewById(R.id.userNameField)
        userPhoneTextView = findViewById(R.id.userPhoneTextView)
        userEmailField = findViewById(R.id.userEmailField)
        userAddressField = findViewById(R.id.userAddressField)

        userPhoneTextView.text = userData.phone

        submitDetailsButton = findViewById(R.id.submitDetailsButton)
        submitDetailsButton.setOnClickListener(this)


    }

    private fun areUserDetailsValid(): Boolean {
        var flag = true
        if (TextUtils.isEmpty(userNameField.text)) {
            flag = false
            userNameField.error = "Invalid name"
        }
        if (TextUtils.isEmpty(userEmailField.text)) {
            flag = false
            userEmailField.error = "Email cannot be empty"
        }
        if (TextUtils.isEmpty(userAddressField.text)) {
            flag = false
            userAddressField.error = "Invalid address"
        }

        return flag
    }


    override fun onClick(v: View?) {
        if (areUserDetailsValid()) {
            userData.name = userNameField.text.toString()
            userData.address = userAddressField.text.toString()
            userData.email = userEmailField.text.toString()
            authRepository.getFirebaseUser()!!.updateEmail(userData.email.toString())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "User email address updated.")
                        authRepository.getFirebaseUser()!!.sendEmailVerification()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d(TAG, "Email sent.")
                                    Snackbar.make(
                                        findViewById(android.R.id.content),
                                        "Please verify your email account to proceed",
                                        Snackbar.LENGTH_LONG
                                    ).show()
                                    authRepository.createEndUserDataObject(userData,
                                        object : AuthRepository.UserDataInterface {
                                            override fun onUploadCallback(success: Boolean) {
                                                if (success) {
                                                    authRepository.createEndUserAuthObject()
                                                    val intent = Intent(
                                                        applicationContext,
                                                        Login::class.java
                                                    )
                                                    startActivity(intent)
                                                    finish()
                                                } else {
                                                    Snackbar.make(
                                                        findViewById(android.R.id.content),
                                                        "Unable to sign you up at this time",
                                                        Snackbar.LENGTH_LONG
                                                    ).show()
                                                }
                                            }
                                        })
                                } else {
                                    Log.d(TAG, "Failure : ${task.result.toString()}")
                                    Snackbar.make(
                                        findViewById(android.R.id.content),
                                        "Email address does not exist",
                                        Snackbar.LENGTH_LONG
                                    ).show()
                                    userEmailField.error = "Invalid email"
                                }
                            }
                    } else {
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "Email address does not exist",
                            Snackbar.LENGTH_LONG
                        ).show()
                        userEmailField.error = "Invalid email"
                    }
                }

        }
    }
}