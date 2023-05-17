package io.timemates.backend.services.authorization

import com.google.protobuf.Empty
import io.grpc.Status
import io.grpc.StatusException
import io.timemates.api.authorizations.AuthorizationServiceGrpcKt
import io.timemates.api.authorizations.requests.ConfirmAuthorizationRequestKt
import io.timemates.api.authorizations.requests.ConfirmAuthorizationRequestOuterClass.ConfirmAuthorizationRequest
import io.timemates.api.authorizations.requests.GetAuthorizationsRequestOuterClass.GetAuthorizationsRequest
import io.timemates.api.authorizations.requests.StartAuthorizationRequestOuterClass.StartAuthorizationRequest
import io.timemates.api.authorizations.types.AuthorizationKt.hash
import io.timemates.api.authorizations.types.authorization
import io.timemates.backend.authorization.types.value.AccessHash
import io.timemates.backend.authorization.types.value.VerificationCode
import io.timemates.backend.authorization.types.value.VerificationHash
import io.timemates.backend.authorization.usecases.*
import io.timemates.backend.pagination.PageToken
import io.timemates.backend.services.authorization.context.provideAuthorizationContext
import io.timemates.backend.services.authorization.interceptor.AuthorizationInterceptor
import io.timemates.backend.services.common.validation.createOrStatus
import io.timemates.backend.users.types.value.EmailAddress

class AuthorizationsService(
    private val authByEmailUseCase: AuthByEmailUseCase,
    private val configureNewAccountUseCase: ConfigureNewAccountUseCase,
    private val refreshTokenUseCase: RefreshTokenUseCase,
    private val removeAccessTokenUseCase: RemoveAccessTokenUseCase,
    private val verifyAuthorizationUseCase: VerifyAuthorizationUseCase,
    private val getAuthorizationsUseCase: GetAuthorizationUseCase,
) : AuthorizationServiceGrpcKt.AuthorizationServiceCoroutineImplBase() {

    override suspend fun confirmAuthorization(
        request: ConfirmAuthorizationRequest,
    ): ConfirmAuthorizationRequest.Response {
        val verificationHash = VerificationHash.createOrStatus(request.verificationHash)
        val confirmationCode = VerificationCode.createOrStatus(request.confirmationCode)

        return when (val result = verifyAuthorizationUseCase.execute(verificationHash, confirmationCode)) {
            VerifyAuthorizationUseCase.Result.AttemptFailed ->
                throw StatusException(Status.INVALID_ARGUMENT)

            VerifyAuthorizationUseCase.Result.AttemptsExceed ->
                throw StatusException(Status.RESOURCE_EXHAUSTED)

            VerifyAuthorizationUseCase.Result.NotFound -> throw StatusException(Status.NOT_FOUND)
            is VerifyAuthorizationUseCase.Result.Success.ExistsAccount ->
                ConfirmAuthorizationRequestKt.response {
                    isNewAccount = false

                    authorization = authorization {
                        accessHash = hash {
                            value = result.authorization.accessHash.string
                            expiresAt = result.authorization.expiresAt.inMilliseconds
                        }
                        refreshHash = hash {
                            value = result.authorization.refreshAccessHash.string
                            expiresAt = result.authorization.expiresAt.inMilliseconds
                        }
                        generationTime = result.authorization.createdAt.inMilliseconds
                    }
                }

            VerifyAuthorizationUseCase.Result.Success.NewAccount ->
                ConfirmAuthorizationRequestKt.response {
                    isNewAccount = true
                }
        }
    }

    override suspend fun getAuthorizations(
        request: GetAuthorizationsRequest,
    ): GetAuthorizationsRequest.Response = throw StatusException(Status.UNIMPLEMENTED)

    override suspend fun startAuthorization(
        request: StartAuthorizationRequest,
    ): Empty {
        val email = EmailAddress.createOrStatus(request.emailAddress)

        return when (authByEmailUseCase.execute(email)) {
            AuthByEmailUseCase.Result.AttemptsExceed -> throw StatusException(Status.RESOURCE_EXHAUSTED)
            AuthByEmailUseCase.Result.SendFailed -> throw StatusException(Status.UNAVAILABLE)
            is AuthByEmailUseCase.Result.Success -> Empty.getDefaultInstance()
        }
    }

    override suspend fun terminateAuthorization(
        request: Empty,
    ): Empty {
        val accessToken = AuthorizationInterceptor.ACCESS_TOKEN_KEY.get()
            .let { AccessHash.createOrStatus(it) }

        return when (removeAccessTokenUseCase.execute(accessToken)) {
            RemoveAccessTokenUseCase.Result.AuthorizationNotFound ->
                throw StatusException(Status.UNAUTHENTICATED)

            RemoveAccessTokenUseCase.Result.Success -> Empty.getDefaultInstance()
        }
    }
}