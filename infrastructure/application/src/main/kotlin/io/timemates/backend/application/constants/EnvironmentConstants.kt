package io.timemates.backend.application.constants

internal object EnvironmentConstants {
    private const val TIME_MATES_PREFIX = "TIMEMATES"
    private const val MAILER_SEND_PREFIX = "MAILERSEND"

    const val APPLICATION_PORT = "APPLICATION_PORT"

    // Database
    const val DATABASE_URL = "${TIME_MATES_PREFIX}_DATABASE_URL"
    const val DATABASE_USER = "${TIME_MATES_PREFIX}_DATABASE_USER"
    const val DATABASE_USER_PASSWORD = "${TIME_MATES_PREFIX}_DATABASE_USER_PASSWORD"

    // SMTP
    const val SMTP_HOST = "${TIME_MATES_PREFIX}_SMTP_HOST"
    const val SMTP_PORT = "${TIME_MATES_PREFIX}_SMTP_PORT"
    const val SMTP_USER = "${TIME_MATES_PREFIX}_SMTP_USER"
    const val SMTP_USER_PASSWORD = "${TIME_MATES_PREFIX}_SMTP_USER_PASSWORD"
    const val SMTP_SENDER_ADDRESS = "${TIME_MATES_PREFIX}_SMTP_SENDER_ADDRESS"

    // MailerSend
    const val MAILER_SEND_API_KEY = "${MAILER_SEND_PREFIX}_API_KEY"
    const val MAILER_SEND_SENDER = "${MAILER_SEND_PREFIX}_SENDER"
    const val MAILER_SEND_CONFIRMATION_TEMPLATE = "${MAILER_SEND_PREFIX}_CONFIRMATION_TEMPLATE"

    // Other constants
    const val FILES_PATH = "${TIME_MATES_PREFIX}_FILES_PATH"
}
