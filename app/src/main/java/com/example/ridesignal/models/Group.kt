package com.example.ridesignal.models

data class Group(
    val groupId: String = "",
    val groupName: String = "",
    val adminUid: String = "",
    val members: List<String> = emptyList(), // List of UIDs
    val createdAt: Long = System.currentTimeMillis()
)