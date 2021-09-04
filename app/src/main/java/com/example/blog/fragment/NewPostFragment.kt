package com.example.blog.fragment

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.example.blog.R
import com.example.blog.data.UploadPost
import com.example.blog.databinding.FragmentNewPostBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import gun0912.tedimagepicker.builder.TedImagePicker
import java.util.*
import kotlin.concurrent.timerTask

class NewPostFragment : Fragment() {

    private lateinit var binding: FragmentNewPostBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var fStore: FirebaseFirestore
    var filePath: Uri? = null
    var urlImage: String? = null
    var id: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNewPostBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.constraint1.visibility = View.GONE

        // khai báo
        mAuth = FirebaseAuth.getInstance()
        fStore = FirebaseFirestore.getInstance()

       // lấy dữ liệu từ profile đẩy lên giao diện
        val db = fStore.collection("Profile")
        Log.d("data", mAuth.currentUser!!.uid)
        db.whereEqualTo("uid", mAuth.currentUser!!.uid).get().addOnSuccessListener { documents ->
            for (document in documents) {
                val db1 = document.data
                binding.tvName.text = db1["uname"].toString()
                Glide.with(requireActivity()).load(db1["uavatar"].toString()).into(binding.ivAvatar)
            }
        }.addOnFailureListener { exception ->
            Log.w("getProfile", "Error getting documents: ", exception)
        }

        // xét sự kiện
        binding.btnImagePicker.setOnClickListener { imagePicker() }
        binding.btnClearImage.setOnClickListener { clearImage() }
        binding.btnPost.setOnClickListener { addPost() }

    }

    private fun turnOffKeyboard() {
        val inputMethodManager = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    private fun imagePicker() {
        activity?.let {
            TedImagePicker.with(it)
                .start { uri ->
                    showSingleImage(uri)
                    filePath = uri
                    binding.constraint1.visibility = View.VISIBLE
                    Log.w("uri", filePath.toString())
                }
        }
    }

    private fun showSingleImage(uri: Uri) {
        activity?.let {
            Glide.with(it).load(uri).into(binding.ivPhoto)
            binding.btnClearImage.visibility = View.VISIBLE
            binding.btnImagePicker.visibility = View.GONE

        }
    }

    private fun clearImage() {
        binding.ivPhoto.setImageResource(0)
        binding.constraint1.visibility = View.GONE
        binding.btnImagePicker.visibility = View.VISIBLE
        filePath = null
    }

    private fun addPost() {
        id = UUID.randomUUID().toString()
        if (filePath != null) {
            uploadImage()
        } else if (!binding.edtTitle.text.isEmpty()) {
            uploadPost()
        } else Snackbar.make(
            binding.root,
            "Please enter a comment or choose an image",
            Snackbar.LENGTH_LONG
        ).show()
    }

    private fun uploadImage() {
        val fStorage = FirebaseStorage.getInstance().getReference("Post/$id")
        fStorage.putFile(filePath!!).addOnSuccessListener {
            fStorage.downloadUrl.addOnSuccessListener {
                urlImage = it.toString()
                uploadPost()
                Log.d("dowloadUrlImage", "Dowload url image success")
            }.addOnFailureListener {
                Log.e("dowloadUrlImage", "Dowload url image failure")
                Snackbar.make(
                    binding.root,
                    "Posted a new post failure. Please try again!",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }.addOnFailureListener() {
            Log.e("uploadImage", "Upload image failure")
            Snackbar.make(
                binding.root,
                "Posted a new post failure. Please try again!",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun uploadPost() {
        val title = binding.edtTitle.text.toString()
        val dataPost =
            UploadPost(
                id,
                mAuth.currentUser!!.uid,
                title,
                urlImage,
                ServerValue.TIMESTAMP,
                ServerValue.TIMESTAMP
            )
        val fDatabase = FirebaseDatabase.getInstance().getReference("Post/$id")
        fDatabase.setValue(dataPost).addOnSuccessListener {
            Log.d("uploadPost","Upload post success")
            turnOffKeyboard()
            Snackbar.make(
                binding.root,
                "Successfully added new post",
                Snackbar.LENGTH_LONG
            ).show()
            Handler().postDelayed(timerTask {
                val viewPager = activity?.findViewById<ViewPager>(R.id.viewPager)
                viewPager!!.currentItem = 0
                restoreDefaultUI()
            },1500)
        }
    }

    private fun restoreDefaultUI() {
        binding.edtTitle.text.clear()
        binding.btnClearImage.visibility = View.GONE
        binding.ivPhoto.visibility = View.GONE
    }

}