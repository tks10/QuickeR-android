package com.qrist.quicker.models

import com.squareup.moshi.JsonClass

sealed class Service(
    open val id: String,
    open val name: String,
    open val iconUrl: String
) {

    @JsonClass(generateAdapter = true)
    data class TwitterService(
        override val id: String
    ) : Service(id = id, name = "Twitter", iconUrl = "twitter_icon")


    @JsonClass(generateAdapter = true)
    data class FacebookService(
        override val id: String
    ) : Service(id = id, name = "Facebook", iconUrl = "facebook_icon")

    @JsonClass(generateAdapter = true)
    data class UserService(
        override val id: String,
        override val name: String,
        override val iconUrl: String
    ) : Service(id = id, name = name, iconUrl = iconUrl)
}
