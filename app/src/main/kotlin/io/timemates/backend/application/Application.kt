@file:Suppress("ExtractKtorModule")

package io.timemates.backend.application

import io.grpc.BindableService
import io.grpc.ServerBuilder
import io.grpc.ServerInterceptor
import io.grpc.protobuf.services.ProtoReflectionService
import io.timemates.backend.application.constants.ArgumentsConstants
import io.timemates.backend.application.constants.EnvironmentConstants
import io.timemates.backend.application.constants.FailureMessages
import io.timemates.backend.application.dependencies.AppModule
import io.timemates.backend.application.dependencies.configuration.DatabaseConfig
import io.timemates.backend.application.dependencies.configuration.MailerConfiguration
import io.timemates.backend.application.dependencies.filesPathName
import io.timemates.backend.cli.getNamedIntOrNull
import io.timemates.backend.cli.parseArguments
import io.timemates.backend.data.common.repositories.MailerSendEmailsRepository
import io.timemates.backend.rsocket.startRSocket
import io.timemates.backend.services.authorization.AuthorizationsService
import io.timemates.backend.services.authorization.interceptor.AuthorizationInterceptor
import io.timemates.backend.services.authorization.interceptor.IpAddressInterceptor
import io.timemates.backend.services.authorization.provider.AuthorizationProvider
import io.timemates.backend.services.files.FilesService
import io.timemates.backend.services.timers.TimersService
import io.timemates.backend.services.timers.sessions.TimerSessionsService
import io.timemates.backend.services.users.UsersService
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import java.net.URI

/**
 * Application entry-point. Next environment variables should be provided:
 *
 * **Environment variables**:
 * - **TIMEMATES_GRPC_PORT**: The port for the gRPC server to listen on. If not provided, defaults to 8080.
 * - **TIMEMATES_RSOCKET_PORT** – The port on which RSocket instance will run (default: `8081`)
 * - **TIMEMATES_DATABASE_URL**: The URL of the database.
 * - **TIMEMATES_DATABASE_USER**: The username for the database connection.
 * - **TIMEMATES_DATABASE_USER_PASSWORD**: The password for the database connection.
 * - **TIMEMATES_SMTP_HOST**: The SMTP host for sending emails.
 * - **TIMEMATES_SMTP_PORT**: The SMTP port for sending emails.
 * - **TIMEMATES_SMTP_USER**: The username for the SMTP server.
 * - **TIMEMATES_SMTP_USER_PASSWORD**: The password for the SMTP server.
 * - **TIMEMATES_SMTP_SENDER_ADDRESS**: The sender email address for outgoing emails.
 * - **MAILERSEND_API_KEY**: The API key for MailerSend service.
 * - **MAILERSEND_SENDER**: The sender email address for MailerSend emails.
 * - **MAILERSEND_RECIPIENT**: The recipient email address for MailerSend emails.
 * - **MAILERSEND_CONFIRMATION_TEMPLATE**: The template ID for MailerSend confirmation emails.
 * - **TIMEMATES_FILES_PATH**: The path for file storage.
 *
 * **Program arguments**:
 *
 * Also, values above can be provided by arguments `grpcPort`, `rsocketPort`, `databaseUrl`, `databaseUser`
 * `databaseUserPassword` and `filesPath`.
 * For example: `java -jar timemates.jar -port 8080 -databaseUrl http..`
 *
 * **Arguments are used first, then environment variables as fallback.**
 * @see ArgumentsConstants
 * @see EnvironmentConstants
 */
suspend fun main(args: Array<String>): Unit = coroutineScope {
    val arguments = args.parseArguments()

    val grpcPort = arguments.getNamedIntOrNull(ArgumentsConstants.GRPC_PORT)
        ?: System.getenv(EnvironmentConstants.GRPC_PORT)?.toIntOrNull()
        ?: 8080

    val rSocketPort = arguments.getNamedIntOrNull(ArgumentsConstants.RSOCKET_PORT)
        ?: System.getenv(EnvironmentConstants.RSOCKET_PORT)?.toIntOrNull()
        ?: 8081

    val databaseUrl = arguments.getNamedOrNull(ArgumentsConstants.DATABASE_URL)
        ?: System.getenv(EnvironmentConstants.DATABASE_URL)
        ?: error(FailureMessages.MISSING_DATABASE_URL)

    val databaseUser = arguments.getNamedOrNull(ArgumentsConstants.DATABASE_USER)
        ?: System.getenv(EnvironmentConstants.DATABASE_USER)
        ?: "".also { println("Database user was not specified, ignoring") }

    val databasePassword = arguments.getNamedOrNull(ArgumentsConstants.DATABASE_USER_PASSWORD)
        ?: System.getenv(EnvironmentConstants.DATABASE_USER_PASSWORD)
        ?: "".also { println("Database password was not specified, ignoring") }

    val databaseConfig = DatabaseConfig(
        url = databaseUrl,
        user = databaseUser,
        password = databasePassword,
    )

    val mailingConfig = if (arguments.isPresent(ArgumentsConstants.SMTP_HOST) ||
        !System.getenv(EnvironmentConstants.SMTP_HOST).isNullOrEmpty()) {
        MailerConfiguration.SMTP(
            host = arguments.getNamedOrNull(ArgumentsConstants.SMTP_HOST)
                ?: System.getenv(EnvironmentConstants.SMTP_HOST)
                ?: error(FailureMessages.MISSING_SMTP_HOST),

            port = arguments.getNamedIntOrNull(ArgumentsConstants.SMTP_PORT)
                ?: System.getenv(EnvironmentConstants.SMTP_PORT)?.toInt()
                ?: error(FailureMessages.MISSING_SMTP_PORT),

            user = arguments.getNamedOrNull(ArgumentsConstants.SMTP_USER)
                ?: System.getenv(EnvironmentConstants.SMTP_USER)
                ?: error(FailureMessages.MISSING_SMTP_USER),

            password = arguments.getNamedOrNull(ArgumentsConstants.SMTP_USER_PASSWORD)
                ?: System.getenv(EnvironmentConstants.SMTP_USER_PASSWORD),

            sender = arguments.getNamedOrNull(ArgumentsConstants.SMTP_SENDER_ADDRESS)
                ?: System.getenv(EnvironmentConstants.SMTP_SENDER_ADDRESS)
                ?: error(FailureMessages.MISSING_SMTP_SENDER),
        )
    } else if (!System.getenv(EnvironmentConstants.MAILERSEND_API_KEY).isNullOrEmpty()
        || arguments.isPresent(ArgumentsConstants.MAILERSEND_API_KEY)) {
        MailerConfiguration.MailerSend(
            configuration = MailerSendEmailsRepository.Configuration(
                apiKey = arguments.getNamedOrNull(ArgumentsConstants.MAILERSEND_API_KEY)
                    ?: System.getenv(EnvironmentConstants.MAILERSEND_API_KEY)
                    ?: error(FailureMessages.MISSING_MAILERSEND_API_KEY),

                sender = arguments.getNamedOrNull(ArgumentsConstants.MAILERSEND_SENDER)
                    ?: System.getenv(EnvironmentConstants.MAILERSEND_SENDER)
                    ?: error(FailureMessages.MISSING_MAILERSEND_SENDER),

                confirmationTemplateId = arguments.getNamedOrNull(ArgumentsConstants.MAILERSEND_CONFIRMATION_TEMPLATE)
                    ?: System.getenv(EnvironmentConstants.MAILERSEND_CONFIRMATION_TEMPLATE)
                    ?: error(FailureMessages.MISSING_MAILERSEND_CONFIRMATION_TEMPLATE),

                supportEmail = arguments.getNamedOrNull(ArgumentsConstants.MAILERSEND_SUPPORT_EMAIL)
                    ?: System.getenv(EnvironmentConstants.MAILERSEND_SUPPORT_EMAIL)
                    ?: error(FailureMessages.MISSING_MAILERSEND_SUPPORT_EMAIL),
            )
        )
    } else {
        error(FailureMessages.MISSING_MAILER)
    }

    val filesPath = arguments.getNamedOrNull(ArgumentsConstants.FILES_PATH)
        ?: System.getenv(EnvironmentConstants.FILES_PATH)
        ?: error(FailureMessages.MISSING_FILES_PATH)

    val dynamicModule = module {
        single<DatabaseConfig> { databaseConfig }
        single<MailerConfiguration> { mailingConfig }
        single(filesPathName) { URI.create("file://$filesPath") }
        singleOf(::AuthorizationProvider)
    }

    val koin = startKoin {
        modules(AppModule + dynamicModule)
    }.koin

    val server = ServerBuilder.forPort(grpcPort)
        .addService(koin.get<UsersService>() as BindableService)
        .addService(koin.get<FilesService>() as BindableService)
        .addService(koin.get<TimersService>() as BindableService)
        .addService(koin.get<AuthorizationsService>() as BindableService)
        .addService(koin.get<TimerSessionsService>() as BindableService)
        .addService(ProtoReflectionService.newInstance())
        .intercept(AuthorizationInterceptor(koin.get()) as ServerInterceptor)
        .intercept(IpAddressInterceptor() as ServerInterceptor)
        .build()

    val rSocketServerJob = launch {
        startRSocket(
            port = rSocketPort,
            authService = koin.get(),
            usersService = koin.get(),
            timersService = koin.get(),
            timerSessionsService = koin.get(),
            timerMembersService = koin.get(),
            timerInvitesService = koin.get(),
            filesService = koin.get(),
            requestsInterceptor = koin.get(),
        )
    }

    server.start()

    Runtime.getRuntime().addShutdownHook(Thread {
        server.shutdown()
        rSocketServerJob.cancel()
        stopKoin()
    })

    server.awaitTermination()
    rSocketServerJob.join()
}
