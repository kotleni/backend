package io.timemates.backend.data.timers.db.entities

import kotlinx.serialization.Serializable

@Serializable
data class InvitesPageToken(val offset: Long)