package com.example.blog

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.example.blog.data.UplaodProfile
import com.example.blog.databinding.ActivitySignupBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var fStore: FirebaseFirestore
    var emailValidation = ("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        mAuth = FirebaseAuth.getInstance()
        fStore = FirebaseFirestore.getInstance()

        // bắt sự kiện cho buton
        binding.tvLogin.setOnClickListener { btnLogin() }
        binding.btnSignUp.setOnClickListener { btnSignUp() }

        // click để tắt phím ảo
        binding.constraint.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val inputMethodManager =
                    getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
            }
        })

    }

    private fun btnSignUp() {
        val email = binding.edtEmail.text.toString()
        val pass = binding.edtPassword.text.toString()
        val passConfirm = binding.edtConfirmPassword.text.toString()
        if(checkValidation()){
            RegisterFirebase(email,pass)
        }

    }

    private fun checkValidation(): Boolean {
        val email = binding.edtEmail.text.toString()
        val pass = binding.edtPassword.text.toString()
        val passConfirm = binding.edtConfirmPassword.text.toString()
        if (email.isEmpty()) {
            binding.tilEmail.setError("Please enter your email")
            return false
        } else {
            binding.tilEmail.error = null
            binding.tilEmail.isErrorEnabled = false
        }

        if (!email.matches(emailValidation.toRegex())) {
            binding.tilEmail.setError("Not a valid email")
            return false
        } else {
            binding.tilEmail.error = null
            binding.tilEmail.isErrorEnabled = false
        }

        if (pass.isEmpty()) {
            binding.tilPassword.setError("Please enter your password")
            return false
        } else {
            binding.tilPassword.error = null
            binding.tilPassword.isErrorEnabled = false
        }
        if (pass.length < 6) {
            binding.tilPassword.setError("The password must be at least 6 characters")
            return false
        } else {
            binding.tilPassword.error = null
            binding.tilPassword.isErrorEnabled = false
        }

        if (passConfirm.isEmpty()) {
            binding.tilConfirmPassword.setError("Please enter your confirm password")
            return false
        } else {
            binding.tilConfirmPassword.error = null
            binding.tilConfirmPassword.isErrorEnabled = false
        }

        if (passConfirm != pass) {
            binding.tilConfirmPassword.setError("Password confirm doesn't match password")
            return false
        } else {
            binding.tilConfirmPassword.error = null
            binding.tilConfirmPassword.isErrorEnabled = false
        }

        return true
    }

    private fun btnLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        finish()
    }

    private fun RegisterFirebase(email: String, pass: String) {
        mAuth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("FirebaseAuthencation", "createUserWithEmail:success")
                    val user = mAuth.currentUser
                    createDataProfile(user)
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    finish()
                }else Log.w("TAG", "createUserWithEmail:failure", task.exception)
                Snackbar.make(binding.root, "Authentication failed.",
                    Snackbar.LENGTH_SHORT).show()
            }
    }

    private fun createDataProfile(user: FirebaseUser?) {
        if (user != null) {
            val email = binding.edtEmail.text.toString()
            val uName = email.substring(0, email.indexOf("@"))
            val uId = user.uid
            val uAvatar =
                "https://firebasestorage.googleapis.com/v0/b/blog-528cf.appspot.com/o/avatar_default.jpg?alt=media&token=a71c5daa-4a2f-426d-adda-64769afd7bbf"
            val data = UplaodProfile(uId, uName, uAvatar, email)
            val db = fStore.collection("Profile").document(uId)
            db.get().addOnSuccessListener {
                if (!it.exists()) {
                    db.set(data).addOnSuccessListener {
                        Log.e(
                            "createFrofile",
                            "success"
                        )// tạo csdl lưu profile cho account thành công
                    }.addOnFailureListener {
                        Log.e(
                            "createFrofile ",
                            "failure"
                        )// tạo csdl lưu profile cho account thất bại
                    }
                } else {
                    Log.e("createFrofile ", "Already have a profile") // đã có csdl lưu profile
                }
            }.addOnFailureListener {
                Log.e("tag4 ", "Error+${it.toString()}")
            }
        }
    }
}