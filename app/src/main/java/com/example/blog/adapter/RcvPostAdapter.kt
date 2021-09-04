package com.example.blog.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blog.CommentActivity
import com.example.blog.R
import com.example.blog.data.ReadPost
import com.example.blog.databinding.ItemRcvHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class RcvPostAdapter(val activity: Context, val postList: ArrayList<ReadPost>) :
    RecyclerView.Adapter<RcvPostAdapter.ViewHolder>() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var fStorage: FirebaseFirestore


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemRcvHomeBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        mAuth = FirebaseAuth.getInstance()

        // lấy dữ liệu từ ArrayList
        val pId = postList[position].idPost
        val uId = postList[position].idUser
        val pTitle = postList[position].title
        val pPhoto = postList[position].photo
        val dateCreate = postList[position].dateCreate
        val dateUpdate = postList[position].dateUpdate
        var uName: String? = null
        var uAvatar: String? = null

        // lấy dữ liệu từ frofile của tài khoản
        fStorage = FirebaseFirestore.getInstance()
        val db = fStorage.collection("Profile")
        Log.d("data", uId!!)

        db.whereEqualTo("uid", uId).get().addOnSuccessListener { documents ->
            for (document in documents) {
                val db1 = document.data
                uName = db1["uname"].toString()
                uAvatar = db1["uavatar"].toString()
                holder.binding.tvUName.text = uName
                Glide.with(activity).load(uAvatar).into(holder.binding.ivAvatar)
                Log.d("data", uName!! + uAvatar)
            }
        }.addOnFailureListener { exception ->
            Log.w("getProfile", "Error getting documents: ", exception)
        }

        // chuyển đổi dateCreate
        val format = SimpleDateFormat("MM/dd/yyyy")
        val date = Date(dateCreate!!)

        // đưa dữ liệu lên giao diện
        holder.binding.ivTitle.text = pTitle
        holder.binding.tvDateCreate.text = format.format(date)
        Glide.with(activity).load(pPhoto).into(holder.binding.ivPhoto)

        // check like
        val like = FirebaseDatabase.getInstance().getReference("LikePost")
        like.addValueEventListener(object : ValueEventListener {
            val Id = mAuth.currentUser!!.uid
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(pId!!).hasChild(Id)) {
                    holder.binding.tvLikeNumber.text =
                        snapshot.child(pId).childrenCount.toString() + " like"
                    holder.binding.checkLike.setImageResource(R.drawable.ic_like_red)
                } else {
                    holder.binding.tvLikeNumber.text =
                        snapshot.child(pId).childrenCount.toString() + " like"
                    holder.binding.checkLike.setImageResource(R.drawable.ic_like_white)

                }
            }

            override fun onCancelled(error: DatabaseError) {}

        })

        holder.binding.btnLike.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val like = FirebaseDatabase.getInstance().getReference("LikePost")
                var checklike = true
                val Id = mAuth.currentUser!!.uid
                like.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (checklike.equals(true)) {
                            if (snapshot.child(pId!!).hasChild(Id)) {
                                like.child(pId!!).child(Id).removeValue()
                                checklike = false
                            } else {
                                like.child(pId!!).child(Id!!).setValue(true)
                                checklike = false
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}

                })
            }

        })
        holder.binding.btnComment.setOnClickListener(object :View.OnClickListener{
            override fun onClick(v: View?) {
                val intent = Intent(activity,CommentActivity::class.java)
                intent.putExtra("pId",pId)
                activity.startActivities(arrayOf(intent))
            }

        })
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    class ViewHolder(val binding: ItemRcvHomeBinding) : RecyclerView.ViewHolder(binding.root) {}
}