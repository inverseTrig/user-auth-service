package com.example.userauthservice

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class TestLogAppender : AppenderBase<ILoggingEvent>() {
    init {
        LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)
            .let { (it as ch.qos.logback.classic.Logger).addAppender(this) }
        start()
    }

    private val events: MutableList<ILoggingEvent> = mutableListOf()

    override fun append(e: ILoggingEvent) {
        events.add(e)
    }

    val lastEvent: ILoggingEvent get() = events.filterNot { it.level == Level.DEBUG }.last()

    fun hasEvent(
        level: Level,
        message: String,
    ): Boolean {
        return events.any { it.level == level && it.message == message }
    }
}
