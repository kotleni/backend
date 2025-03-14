package io.timemates.backend.services.users

import io.timemates.api.users.requests.EditUserRequestOuterClass
import io.timemates.api.users.types.UserOuterClass
import io.timemates.api.users.types.user
import io.timemates.backend.files.types.value.FileId
import io.timemates.backend.services.common.markers.GrpcMapper
import io.timemates.backend.services.common.validation.createOrStatus
import io.timemates.backend.users.types.Avatar
import io.timemates.backend.users.types.value.EmailAddress
import io.timemates.backend.users.types.value.UserDescription
import io.timemates.backend.users.types.value.UserName
import io.timemates.backend.users.types.User as DomainUser

class GrpcUsersMapper : GrpcMapper {
    fun toGrpcUser(domain: DomainUser): UserOuterClass.User {
        return user {
            id = domain.id.long
            name = domain.name.string
            domain.emailAddress?.string?.let { email = it }
            domain.description?.string?.let { description = it }
            when (val avatar = domain.avatar) {
                is Avatar.GravatarId -> gravatarId = avatar.string
                is Avatar.FileId -> avatarId = avatar.string
                else -> {}
            }
        }
    }

    fun toGrpcUserPatch(patch: EditUserRequestOuterClass.EditUserRequest): DomainUser.Patch {
        return DomainUser.Patch(
            name = patch.name.takeIf { patch.hasName() }
                ?.let { UserName.createOrStatus(it) },
            description = patch.description.takeIf { patch.hasDescription() }
                ?.let { UserDescription.createOrStatus(it) },
            avatar = if(patch.hasAvatarId())
                Avatar.FileId.createOrStatus(patch.avatarId)
            else Avatar.GravatarId.createOrStatus(patch.gravatarId)
        )
    }
}