package com.example.blog

import android.content.Intent
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import com.example.blog.data.UplaodProfile
import com.example.blog.databinding.ActivityLoginBinding
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class LoginActivity : AppCompatActivity() {
    companion object {
        private const val RC_SIGN_IN = 120
        private const val TAG = "Google Sign In "
    }

    private lateinit var binding: ActivityLoginBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager
    private lateinit var fStore: FirebaseFirestore

    var emailValidation = ("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // bắt sự kiện cho các button
        binding.tvSignUp.setOnClickListener { Signup() }
        binding.btnLogIn.setOnClickListener { Login() }
        binding.btnLoginGoogle.setOnClickListener { LoginGoogle() }

        LoginFacebook()

        // click để tắt bàn phím ảo
        binding.constraint.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val inputMethodManager =
                    getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
            }

        })

        mAuth = FirebaseAuth.getInstance()
        fStore = FirebaseFirestore.getInstance()

        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

    }

    private fun Signup() {
        val intent = Intent(this, SignupActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        finish()
    }

    override fun onStart() {
        super.onStart()
        checkConnect()
        val currentUser = mAuth.currentUser
        if (currentUser != null) {
            updateUI(currentUser)
        }
    }

    private fun checkConnect() {
        if (!isConnected()) Handler().postDelayed({ buildDialog()?.show() }, 1300)
    }

    //check kết nối internet
    fun isConnected(): Boolean {
        val connectivityManager =
            this.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        if (networkInfo != null && networkInfo.isConnectedOrConnecting) {
            val wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            val mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
            return mobile != null && mobile.isConnectedOrConnecting || wifi != null && wifi.isConnectedOrConnecting
        }
        return false
    }

    // thông báo chưa kết nối internet
    fun buildDialog(): AlertDialog.Builder? {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("No Internet Connection")
        builder.setPositiveButton("Ok") { dialog, which ->
            startActivity(Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
        }
        return builder
    }


    private fun updateUI(currentUser: Any?) {
        if (currentUser != null) {
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // đăng nhập bằng email/pass
    private fun Login() {
        val email = binding.edtEmail.text.toString()
        val pass = binding.edtPassword.text.toString()
        if (ckeckValidation()) {
            mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d("TAG", "signInWithEmail:success")
                        val user = mAuth.currentUser
                        Log.d("tag", user?.uid.toString())
                        updateUI(mAuth)
                    } else {
                        Log.w("TAG", "signInWithEmail:failure", task.exception)
                        Snackbar.make(
                            binding.root,
                            "Email or password is incorrect. Please check again",
                            Snackbar.LENGTH_LONG
                        )
                            .show()
                        updateUI(null)
                    }
                }
        }

    }

    // kiểm tra đầu vào của email và pass
    private fun ckeckValidation(): Boolean {
        val email = binding.edtEmail.text.toString()
        val pass = binding.edtPassword.text.toString()
        if (email.isEmpty()) {
            binding.tilEmail.error = "Please enter your email"
            return false
        } else {
            binding.tilEmail.error = null
            binding.tilEmail.isErrorEnabled = false
        }

        if (pass.isEmpty()) {
            binding.tilPassword.error = "Please enter your password"
            return false
        } else {
            binding.tilPassword.isErrorEnabled = false
            binding.tilPassword.error = null
        }

        return true
    }

    // đăng nhập bằng gg
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            Log.d(TAG, "onActivityResult : Google SignIn intent result")
            val accountTask = GoogleSignIn.getSignedInAccountFromIntent(data)
            var exception = accountTask.exception
            if (accountTask.isSuccessful) {
                try {
                    val account = accountTask.getResult(ApiException::class.java)
                    Log.d(TAG, "firebaseAuthWithGoogle:" + account!!.id)
                    firebaseAuthWithGoogle(account.idToken)
                } catch (e: ApiException) {
                    Log.d(TAG, "Google sign in failed", e)
                }
            } else {
                Log.w(TAG, exception.toString())
            }
        }
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun LoginGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val user = mAuth.currentUser
                    CheckDataProfile(user)
                    updateUI(user)
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }

    // kiểm tra xem account fb, gg đã có profile chưa -> chưa thì tạo mới
    private fun CheckDataProfile(user: FirebaseUser?) {
        val data = UplaodProfile(
            user!!.uid,
            user.displayName,
            user.photoUrl.toString(),
            user.email,
            user.phoneNumber
        )
        val db = fStore.collection("Profile").document(user.uid)
        db.get().addOnSuccessListener {
            if (!it.exists()) {
                db.set(data).addOnSuccessListener {
                    Log.e("createFrofile", "success")// tạo csdl lưu profile cho account thành công
                }.addOnFailureListener {
                    Log.e("createFrofile ", "failure")// tạo csdl lưu profile cho account thất bại
                }
            } else {
                Log.e("createFrofile ", "Already have a profile") // đã có csdl lưu profile
            }
        }.addOnFailureListener {
            Log.e("tag4 ", "Error+${it.toString()}")
        }
    }

    // Đăng nhập bằng account fb
    private fun LoginFacebook() {
        callbackManager = CallbackManager.Factory.create()
        binding.btnLoginFacebook.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                LoginManager.getInstance().logInWithReadPermissions(
                    this@LoginActivity, Arrays.asList("email", "public_profile")
                )
                LoginManager.getInstance().registerCallback(callbackManager,
                    object : FacebookCallback<LoginResult> {
                        override fun onSuccess(loginResult: LoginResult) {
                            handleFacebookAccessToken(loginResult.accessToken)
                            Log.d("tag", loginResult.accessToken.toString())
                        }

                        override fun onCancel() {}
                        override fun onError(error: FacebookException) {}
                    })
            }
        })
    }


    private fun handleFacebookAccessToken(accessToken: AccessToken?) {
        val credential = FacebookAuthProvider.getCredential(accessToken!!.token)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = mAuth.currentUser
                    CheckDataProfile(user)
                    updateUI(user)
                } else {
                    Snackbar.make(
                        binding.root,
                        "Authentication failed.",
                        Snackbar.LENGTH_LONG
                    )
                        .show()
                    updateUI(null)
                }
            }
    }

}