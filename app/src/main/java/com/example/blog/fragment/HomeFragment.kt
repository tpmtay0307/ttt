package com.example.blog.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.blog.adapter.RcvPostAdapter
import com.example.blog.data.ReadPost
import com.example.blog.databinding.FragmentHomeBinding
import com.google.firebase.database.*

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var fDatabase: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = activity as Context

        binding.rcvHome.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        (binding.rcvHome.layoutManager as LinearLayoutManager).reverseLayout = true
        (binding.rcvHome.layoutManager as LinearLayoutManager).stackFromEnd = true
        binding.rcvHome.setHasFixedSize(true)
        fDatabase = FirebaseDatabase.getInstance().getReference("Post")
        fDatabase.orderByChild("dateCreate").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val postList = ArrayList<ReadPost>()
                if (snapshot.exists()) {
                    for (pSnapshot in snapshot.children) {
                        val data = pSnapshot.getValue(ReadPost::class.java)
                        postList.add(data!!)
                    }
                }
                binding.rcvHome.adapter = RcvPostAdapter(activity, postList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DataPost", error.message)
            }

        })


    }

}