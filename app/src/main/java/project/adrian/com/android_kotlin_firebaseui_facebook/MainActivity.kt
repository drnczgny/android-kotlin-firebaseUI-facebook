package project.adrian.com.android_kotlin_firebaseui_facebook

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private val RC_SIGN_IN = 9001
    }

    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setUpButtons()
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                Log.e(TAG, "succesful login");
                refresh()
//                startActivity()
//                finish()
                return
            } else {
                if (response == null) {
                    // User pressed back button
                    Toast.makeText(this, "sign in cancelled", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "sign in cancelled");
                    return
                }

                if (response.errorCode == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(this, "no internet connection", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "no internet connection");
                    return
                }

                if (response.errorCode == ErrorCodes.UNKNOWN_ERROR) {
                    Toast.makeText(this, "unknown error", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "unknown error");
                    return
                }
            }
            Toast.makeText(this, "unknown_sign_in_response", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "unknown_sign_in_response");
        }
    }

    private fun setUpButtons() {
        btnLogin.setOnClickListener {
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(getSignInProviders())
                            .setIsSmartLockEnabled(false, true)
                            .setAllowNewEmailAccounts(true)
                            .build(),
                    RC_SIGN_IN)
        }

        btnLogout.setOnClickListener {
            AuthUI.getInstance().signOut(this)
            refresh()
            Toast.makeText(this, "logged out", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "logged out");
        }
    }

    private fun refresh() {
        updateText()
        updateButtons()
    }

    private fun isLoggedUser() = firebaseAuth.currentUser != null

    private fun updateButtons() {
        if (isLoggedUser()) {
            btnLogin.isEnabled = false
            btnLogout.isEnabled = true
        } else {
            btnLogin.isEnabled = true
            btnLogout.isEnabled = false
        }
    }

    private fun updateText() {
        if (isLoggedUser()) {
            tvSignedInStatus.text = "Signed in as: ${firebaseAuth.currentUser?.displayName}"
        } else {
            tvSignedInStatus.text = "Signed out"
        }
    }

    private fun getSignInProviders(): MutableList<AuthUI.IdpConfig> {
        val selectedProviders = ArrayList<AuthUI.IdpConfig>()
        selectedProviders.add(AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build())
        selectedProviders.add(AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).setPermissions(listOf("email", "public_profile", "user_friends")).build())
//        selectedProviders.add(AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build())
//        selectedProviders.add(AuthUI.IdpConfig.Builder(AuthUI.TWITTER_PROVIDER).build())
//        selectedProviders.add(AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build())
        return selectedProviders
    }
}
