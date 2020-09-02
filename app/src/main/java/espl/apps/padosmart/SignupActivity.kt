package espl.apps.padosmart

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import espl.apps.padosmart.bases.EndUserBase
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

        userData = intent.getParcelableExtra(getString(R.string.userDataParcel))

        userNameField = findViewById(R.id.userNameField)
        userPhoneTextView = findViewById(R.id.userPhoneTextView)
        userEmailField = findViewById(R.id.userEmailField)
        userAddressField = findViewById(R.id.userAddressField)

        userPhoneTextView.text = userData.phone

        submitDetailsButton = findViewById(R.id.submitDetailsButton)
        submitDetailsButton.setOnClickListener(this)


    }

    fun areUserDetailsValid(): Boolean {
        var flag = true
        if (TextUtils.isEmpty(userNameField.text)) {
            flag = false
            userNameField.setHintTextColor(Color.parseColor("#FF0000"))
        }
        if (TextUtils.isEmpty(userEmailField.text)) {
            flag = false
            userEmailField.setHintTextColor(Color.parseColor("#FF0000"))
        }
        if (TextUtils.isEmpty(userAddressField.text)) {
            flag = false
            userEmailField.setHintTextColor(Color.parseColor("#FF0000"))
        }
        if (TextUtils.isEmpty(userEmailField.text)) {
            flag = false
            userEmailField.setHintTextColor(Color.parseColor("#FF0000"))
        }

        return flag
    }


    override fun onClick(v: View?) {
        if (areUserDetailsValid()) {
            userData.name = userNameField.text.toString()
            userData.address = userAddressField.text.toString()
            userData.email = userEmailField.text.toString()
            authRepository.createEndUserDataObject(userData,
                object : AuthRepository.UserDataInterface {
                    override fun onUploadCallback(success: Boolean) {
                        if (success) {
                            val intent = Intent(applicationContext, EndUserBase::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Snackbar.make(
                                findViewById(R.id.content), "Unable to sign you up at this time",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }
                })
        }
    }
}