package com.hometech.mediaprocessor.configuration

import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.ApplicationEventMulticaster
import org.springframework.context.event.EventListener
import org.springframework.context.event.SmartApplicationListener

@Configuration
class EventListenerConfiguration(val eventMulticaster: ApplicationEventMulticaster) {

    @EventListener(ApplicationStartedEvent::class)
    fun disableEventListener() {
        eventMulticaster.removeApplicationListeners {
            it is SmartApplicationListener && it.listenerId.startsWith("com.hometech.mediaprocessor")
        }
    }
}
