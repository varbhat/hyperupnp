/*
 * Copyright (C) 2013 4th Line GmbH, Switzerland
 *
 * The contents of this file are subject to the terms of either the GNU
 * Lesser General Public License Version 2 or later ("LGPL") or the
 * Common Development and Distribution License Version 1 or later
 * ("CDDL") (collectively, the "License"). You may not use this file
 * except in compliance with the License. See LICENSE.txt for more
 * information.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package app.vbt.hyperupnp.androidupnp

import app.vbt.hyperupnp.upnp.cling.transport.impl.NetworkAddressFactoryImpl
import app.vbt.hyperupnp.upnp.cling.transport.spi.InitializationException
import java.lang.reflect.Field
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.logging.Level
import java.util.logging.Logger

/**
 * This factory tries to work around and patch some Android bugs.
 *
 * @author Michael Pujos
 * @author Christian Bauer
 * @author Var Bhat
 */
class AndroidNetworkAddressFactory(streamListenPort: Int) :
    NetworkAddressFactoryImpl(streamListenPort) {
    override fun requiresNetworkInterface(): Boolean {
        return false
    }

    override fun isUsableAddress(
        networkInterface: NetworkInterface,
        address: InetAddress
    ): Boolean {
        val result = super.isUsableAddress(networkInterface, address)
        if (result) {
            // TODO: Workaround Android DNS reverse lookup issue, still a problem on ICS+?
            // http://4thline.org/projects/mailinglists.html#nabble-td3011461
            val hostName = address.hostAddress
            var field0: Field?
            var target: Any?
            try {
                try {
                    field0 = InetAddress::class.java.getDeclaredField("holder")
                    field0.isAccessible = true
                    target = field0[address]
                    field0 = target.javaClass.getDeclaredField("hostName")
                } catch (e: NoSuchFieldException) {
                    // Let's try the non-OpenJDK variant
                    field0 = InetAddress::class.java.getDeclaredField("hostName")
                    target = address
                }
                if (field0 != null && target != null && hostName != null) {
                    field0.isAccessible = true
                    field0[target] = hostName
                } else {
                    return false
                }
            } catch (ex: Exception) {
                log.log(
                    Level.SEVERE,
                    "Failed injecting hostName to work around Android InetAddress DNS bug: $address",
                    ex
                )
                return false
            }
        }
        return result
    }

    override fun getLocalAddress(
        networkInterface: NetworkInterface,
        isIPv6: Boolean,
        remoteAddress: InetAddress
    ): InetAddress {
        // TODO: This is totally random because we can't access low level InterfaceAddress on Android!
        for (localAddress in getInetAddresses(networkInterface)) {
            if (isIPv6 && localAddress is Inet6Address) return localAddress
            if (!isIPv6 && localAddress is Inet4Address) return localAddress
        }
        throw IllegalStateException("Can't find any IPv4 or IPv6 address on interface: " + networkInterface.displayName)
    }

    @Throws(InitializationException::class)
    override fun discoverNetworkInterfaces() {
        try {
            super.discoverNetworkInterfaces()
        } catch (ex: Exception) {
            // TODO: ICS bug on some models with network interface disappearing while enumerated
            // http://code.google.com/p/android/issues/detail?id=33661
            log.warning("Exception while enumerating network interfaces, trying once more: $ex")
            super.discoverNetworkInterfaces()
        }
    }

    companion object {
        private val log = Logger.getLogger(
            AndroidUpnpServiceConfiguration::class.java.name
        )
    }
}