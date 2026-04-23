package com.example.ridesignal.models

import com.google.firebase.firestore.PropertyName

/**
 * This class represents the "Signal" sent between riders.
 * It must have default values (like = "" or = 0L) so Firebase can
 * recreate the object when it arrives on another phone.
 */
data class SignalMessage(

    @get:PropertyName("senderId") @set:PropertyName("senderId")
    var senderId: String = "",

    @get:PropertyName("senderName") @set:PropertyName("senderName")
    var senderName: String = "",

    @get:PropertyName("signalType") @set:PropertyName("signalType")
    var signalType: String = "",

    @get:PropertyName("colorHex") @set:PropertyName("colorHex")
    var colorHex: String = "#EF4444",

    @get:PropertyName("iconName") @set:PropertyName("iconName")
    var iconName: String = "ic_stop",

    @get:PropertyName("timestamp") @set:PropertyName("timestamp")
    var timestamp: Long = 0L
)