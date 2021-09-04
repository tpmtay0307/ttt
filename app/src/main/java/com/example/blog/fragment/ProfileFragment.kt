package com.example.blog.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.blog.LoginActivity
import com.example.blog.adapter.RcvPostAdapter
import com.example.blog.data.ReadPost
import com.example.blog.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var fStore: FirebaseFirestore
    private lateinit var ref: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = activity as Context

        mAuth = FirebaseAuth.getInstance()
        fStore = FirebaseFirestore.getInstance()


        val db = fStore.collection("Profile")
        db.whereEqualTo("uid", mAuth.currentUser!!.uid).get().addOnSuccessListener { documents ->
            for (document in documents) {
                val db1 = document.data
                binding.tvName.text = db1["uname"].toString()
                Glide.with(requireActivity()).load(db1["uavatar"].toString()).into(binding.ivAvatar)
            }
        }.addOnFailureListener { exception ->
            Log.w("getProfile", "Error getting documents: ", exception)
        }

        // xét sự kiện cho button Login
        binding.btnLogout.setOnClickListener {
            val builder = AlertDialog.Builder(activity)
            builder.setTitle("Log out")
            builder.setMessage("Do you want to log out")
            builder.setPositiveButton("Yes") { dialogInterface: DialogInterface, i: Int ->
                mAuth = FirebaseAuth.getInstance()
                mAuth.signOut()
                val intent = Intent(activity, LoginActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
            builder.setNegativeButton("Cancel") { dialogInterface: DialogInterface, i: Int -> }
            builder.show()
        }

        // xử lí recycleview (id : rcv_profile)
        binding.rcvProfile.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        (binding.rcvProfile.layoutManager as LinearLayoutManager).reverseLayout = true
        (binding.rcvProfile.layoutManager as LinearLayoutManager).stackFromEnd = true
        binding.rcvProfile.setHasFixedSize(true)
        val fDatabase = FirebaseDatabase.getInstance().getReference("Post")
        fDatabase.orderByChild("idUser").equalTo(mAuth.currentUser!!.uid)
        val mypost = fDatabase.orderByChild("dateCreate")
        mypost.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val postList = ArrayList<ReadPost>()
                if (snapshot.exists()) {
                    for (pSnapshot in snapshot.children) {
                        val data = pSnapshot.getValue(ReadPost::class.java)
                        postList.add(data!!)
                    }
                }
                binding.rcvProfile.adapter = RcvPostAdapter(activity, postList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DataPost", error.message)
            }

        })
    }

}