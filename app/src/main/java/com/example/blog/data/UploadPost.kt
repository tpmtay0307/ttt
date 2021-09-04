package com.example.blog.data

data class UploadPost(
    val idPost: String? = null,
    val idUser: String? = null,
    val title: String? = null,
    val photo: String? = null,
    val dateCreate: MutableMap<String, String>? = null,
    val dateUpdate: MutableMap<String, String>? = null
)

