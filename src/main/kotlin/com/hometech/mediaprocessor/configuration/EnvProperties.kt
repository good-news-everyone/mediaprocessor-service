package com.hometech.mediaprocessor.configuration

import java.net.InetAddress

val hostOs = Os.resolve()

val hostAddress: String = InetAddress.getLocalHost().let { "${it.hostName}/${it.hostAddress}" }

enum class Os {
    WINDOWS, MAC, UNIX;

    companion object {
        fun resolve(): Os {
            val system = System.getProperty("os.name")
            return when {
                system.contains("windows", ignoreCase = true) -> WINDOWS
                system.contains("mac os", ignoreCase = true) -> MAC
                else -> UNIX
            }
        }
    }
}
