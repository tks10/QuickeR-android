package com.qrist.quicker.models

import com.squareup.moshi.JsonClass

// if the service is Default one, icon is gonna be included in this app.
sealed class Service(
    open val id: String,
    open val name: String
) {

    @JsonClass(generateAdapter = true)
    data class TwitterService(
        override val id: String
    ) : Service(id = id, name = "Twitter")


    @JsonClass(generateAdapter = true)
    data class FacebookService(
        override val id: String
    ) : Service(id = id, name = "Facebook")

    data class LineService(
        override val id: String
    ) : Service(id = id, name = "LINE")

    @JsonClass(generateAdapter = true)
    data class UserService(
        override val id: String,
        override val name: String,
        val iconUrl: String
    ) : Service(id = id, name = name)
}
