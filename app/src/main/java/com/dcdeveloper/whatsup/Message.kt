package com.dcdeveloper.whatsup

import com.google.firebase.database.ServerValue.TIMESTAMP
import com.google.firebase.database.Exclude


class Message {

    var message: String? = null
    var senderId: String? = null
    var recipientId: String? = null
    var id: String? = null
    var timestampCreated: Any? = null

    constructor(message: String?, senderId: String?, recipientId: String?, id: String?) {
        this.message = message
        this.senderId = senderId
        this.recipientId = recipientId
        this.id = id
        this.timestampCreated = TIMESTAMP
    }
    constructor() :
            this("", "",
        "", ""
    )

    @Exclude
    fun getCreatedTimestampLong(): Long {
        return timestampCreated as Long
    }

}