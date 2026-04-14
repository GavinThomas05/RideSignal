package com.example.ridesignal.models

data class FriendRequest(
    val fromUid: String = "",
    val fromName: String = "",
    val fromFriendCode: String = "",
    val requestId: String = "" // document ID
)