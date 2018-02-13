package project.adrian.com.android_kotlin_firebaseui_facebook

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*




class MainActivity : AppCompatActivity() {

    private val TAG = "my_TAG"

    private val RC_SIGN_IN = 9001

    val firebaseAuth = FirebaseAuth.getInstance()

    var callbackManager = CallbackManager.Factory.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setUpButtons()

        try {
            val info = packageManager.getPackageInfo(
                    "project.adrian.com.android_kotlin_firebaseui_facebook",
                    PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.d("KeyHash:", Base64.getEncoder().encodeToString(md.digest()))
            }
        } catch (e: PackageManager.NameNotFoundException) {

        } catch (e: NoSuchAlgorithmException) {

        }


//        LoginManager.getInstance().registerCallback(callbackManager,
//                object : FacebookCallback<LoginResult> {
//                    override fun onSuccess(loginResult: LoginResult) {
//                        Log.e(TAG, "onSuccess");
////                        if (AccessToken.getCurrentAccessToken() == null) {
////                            LoginManager.getInstance().logInWithReadPermissions(this@MainActivity, listOf("public_profile"));
////                        }
//                        val credential = FacebookAuthProvider.getCredential(AccessToken.getCurrentAccessToken().token)
//                        firebaseAuth.signInWithCredential(credential)
//                    }
//
//                    override fun onCancel() {
//                        Log.e(TAG, "onCancel");
//                    }
//
//                    override fun onError(exception: FacebookException) {
//                        Log.e(TAG, "onError");
//                    }
//                })

    }

    override fun onResume() {
        super.onResume()
        if (firebaseAuth.currentUser != null) {
            tvSignedInStatus.text = "signed in as -> ${firebaseAuth.currentUser?.displayName}"
        } else {
            tvSignedInStatus.text = "NOT signed in"
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data)
        // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            // Successfully signed in
            if (resultCode == Activity.RESULT_OK) {
//                startActivity(SignedInActivity.createIntent(this, response))
                Log.e(TAG, "succesful login");
                finish()
                return
            } else {
                // Sign in failed
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
        val facebookIdp: AuthUI.IdpConfig = AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER)
                .build();

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
        }

        btnGoogleLogin.setOnClickListener {
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(getGoogleSignInProviders())
                            .setIsSmartLockEnabled(false, true)
                            .setAllowNewEmailAccounts(true)
                            .build(),
                    RC_SIGN_IN)
        }

        loginButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.e(TAG, "onSuccess");
//                val credential = FacebookAuthProvider.getCredential(AccessToken.getCurrentAccessToken().token)
                val credential = FacebookAuthProvider.getCredential(loginResult.accessToken.token)
                firebaseAuth.signInWithCredential(credential)
                        .addOnCompleteListener { task: Task<AuthResult> ->
                            if (task.isSuccessful) {
                                //Registration OK
                                Log.d(TAG, "signInWithCredential:success");
                                val firebaseUser = firebaseAuth.currentUser!!
                                // handle your UI
                            } else {
                                Log.w(TAG, "signInWithCredential:failure", task.getException());
                                // handle your UI
                            }

//                        .addOnCompleteListener(this, object : OnCompleteListener<AuthResult> {
//                            fun onComplete(task: Task<AuthResult>) {
//                                if (task.isSuccessful()) {
//                                    // Sign in success, update UI with the signed-in user's information
//                                    Log.d(TAG, "signInWithCredential:success")
//                                    val user = firebaseAuth.getCurrentUser()
////                                    updateUI(user)
//                                } else {
//                                    // If sign in fails, display a message to the user.
//                                    Log.w(TAG, "signInWithCredential:failure", task.getException())
//                                    Toast.makeText(this@MainActivity, "Authentication failed.",
//                                            Toast.LENGTH_SHORT).show()
////                                    updateUI(null)
//                                }
//
//                                // ...
//                            }
//                        })
//                firebaseAuth.signInWithCustomToken(AccessToken.getCurrentAccessToken().token)
//                firebaseAuth.signInWithCustomToken(loginResult.accessToken.token)
                        }
            }

            override fun onCancel() {
                Log.e(TAG, "onCancel");
            }

            override fun onError(exception: FacebookException) {
                Log.e(TAG, "onError");
            }
        })
    }

    private fun getSignInProviders(): MutableList<AuthUI.IdpConfig> {
        val selectedProviders = ArrayList<AuthUI.IdpConfig>()
//        selectedProviders.add(AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build())
        selectedProviders.add(AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build())
        selectedProviders.add(AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).setPermissions(listOf("email", "public_profile", "user_friends")).build())
//        selectedProviders.add(AuthUI.IdpConfig.Builder(AuthUI.TWITTER_PROVIDER).build())
//        selectedProviders.add(AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build())
        return selectedProviders
    }

    private fun getGoogleSignInProviders(): MutableList<AuthUI.IdpConfig> {
        val selectedProviders = ArrayList<AuthUI.IdpConfig>()
//        selectedProviders.add(AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build())
        selectedProviders.add(AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build())
//        selectedProviders.add(AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).setPermissions(listOf("email", "public_profile", "user_friends")).build())
//        selectedProviders.add(AuthUI.IdpConfig.Builder(AuthUI.TWITTER_PROVIDER).build())
//        selectedProviders.add(AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build())
        return selectedProviders
    }
}
