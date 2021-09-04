package com.example.blog.data

data class ReadPost(
    val idPost: String? = null,
    val idUser: String? = null,
    val title: String? = null,
    val photo: String? = null,
    val dateCreate: Long? = null,
    val dateUpdate: Long? = null
)
