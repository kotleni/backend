package io.timemates.backend.data.authorization.cache.mapper

import io.timemates.backend.validation.createOrThrowInternally
import io.timemates.backend.authorization.types.metadata.ClientMetadata
import io.timemates.backend.authorization.types.metadata.value.ClientIpAddress
import io.timemates.backend.authorization.types.metadata.value.ClientName
import io.timemates.backend.authorization.types.metadata.value.ClientVersion
import io.timemates.backend.data.authorization.cache.entities.CacheAuthorization
import io.timemates.backend.data.authorization.db.entities.DbAuthorization
import io.timemates.backend.data.common.markers.Mapper

class CacheAuthorizationsMapper : Mapper {
    fun dbToCacheAuthorization(auth: DbAuthorization): CacheAuthorization = with(auth) {
        CacheAuthorization(
            userId = userId,
            accessHash = accessHash,
            refreshAccessHash = refreshAccessHash,
            permissions = dbPermissionsToCachePermissions(permissions),
            expiresAt = expiresAt,
            createdAt = createdAt,
            clientMetadata = ClientMetadata(
                clientName = ClientName.createOrThrowInternally(metaClientName),
                clientVersion = ClientVersion.createOrThrowInternally(metaClientVersion),
                clientIpAddress = ClientIpAddress.createOrThrowInternally(metaClientIpAddress),
            )
        )
    }

    private fun dbPermissionsToCachePermissions(
        permissions: DbAuthorization.Permissions,
    ): CacheAuthorization.Permissions = with(permissions) {
        CacheAuthorization.Permissions(
            authorization = authorization.toCacheGrantLevel(),
            users = users.toCacheGrantLevel(),
            files = files.toCacheGrantLevel(),
            timers = timers.toCacheGrantLevel(),
        )
    }

    private fun DbAuthorization.Permissions.GrantLevel.toCacheGrantLevel(): CacheAuthorization.Permissions.GrantLevel =
        when (this) {
            DbAuthorization.Permissions.GrantLevel.READ -> CacheAuthorization.Permissions.GrantLevel.READ
            DbAuthorization.Permissions.GrantLevel.WRITE -> CacheAuthorization.Permissions.GrantLevel.WRITE
            DbAuthorization.Permissions.GrantLevel.NOT_GRANTED -> CacheAuthorization.Permissions.GrantLevel.NOT_GRANTED
        }
}