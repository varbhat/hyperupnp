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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.MulticastLock
import android.net.wifi.WifiManager.WifiLock
import app.vbt.hyperupnp.upnp.cling.UpnpServiceConfiguration
import app.vbt.hyperupnp.upnp.cling.protocol.ProtocolFactory
import app.vbt.hyperupnp.upnp.cling.transport.Router
import app.vbt.hyperupnp.upnp.cling.transport.RouterException
import app.vbt.hyperupnp.upnp.cling.transport.RouterImpl
import java.util.logging.Logger

/**
 * Monitors all network connectivity changes, switching the router accordingly.
 *
 * @author Michael Pujos
 * @author Christian Bauer
 * @author Var Bhat
 */
open class AndroidRouter(
    configuration: UpnpServiceConfiguration?,
    protocolFactory: ProtocolFactory?,
    private val context: Context
) : RouterImpl(configuration, protocolFactory) {
    private val wifiManager: WifiManager =
        context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private var multicastLock: MulticastLock? = null
    private var wifiLock: WifiLock? = null

    override fun getLockTimeoutMillis(): Int = 15000

    private val isWifi: Boolean
        get() {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        }

    @Throws(RouterException::class)
    override fun shutdown() {
        super.shutdown()
        unregisterNetworkCallbacks()
    }

    fun unregisterNetworkCallbacks() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
        try {
            context.unregisterReceiver(hotSpotReceiver)
        } catch (e: Exception) {
            log.warning(e.toString())
        }

    }

    @Throws(RouterException::class)
    override fun enable(): Boolean {
        lock(writeLock)
        return try {
            var enabled: Boolean
            if (super.enable().also { enabled = it }) {
                // Enable multicast on the WiFi network interface,
                // requires android.permission.CHANGE_WIFI_MULTICAST_STATE
                if (isWifi) {
                    setWiFiMulticastLock(true)
                    setWifiLock(true)
                }
            }
            enabled
        } finally {
            unlock(writeLock)
        }
    }

    @Throws(RouterException::class)
    override fun disable(): Boolean {
        lock(writeLock)
        return try {
            // Disable multicast on WiFi network interface,
            // requires android.permission.CHANGE_WIFI_MULTICAST_STATE
            if (isWifi) {
                setWiFiMulticastLock(false)
                setWifiLock(false)
            }
            super.disable()
        } finally {
            unlock(writeLock)
        }
    }


    private fun setWiFiMulticastLock(enable: Boolean) {
        if (multicastLock == null) {
            multicastLock = wifiManager.createMulticastLock(javaClass.simpleName)
        }
        if (enable) {
            if (multicastLock!!.isHeld) {
                log.warning("WiFi multicast lock already acquired")
            } else {
                log.info("WiFi multicast lock acquired")
                multicastLock!!.acquire()
            }
        } else {
            if (multicastLock!!.isHeld) {
                log.info("WiFi multicast lock released")
                multicastLock!!.release()
            } else {
                log.warning("WiFi multicast lock already released")
            }
        }
    }

    private fun setWifiLock(enable: Boolean) {
        if (wifiLock == null) {
            wifiLock = wifiManager.createWifiLock(
                WifiManager.WIFI_MODE_FULL_HIGH_PERF,
                javaClass.simpleName
            )
        }
        if (enable) {
            if (wifiLock!!.isHeld) {
                log.warning("WiFi lock already acquired")
            } else {
                log.info("WiFi lock acquired")
                wifiLock!!.acquire()
            }
        } else {
            if (wifiLock!!.isHeld) {
                log.info("WiFi lock released")
                wifiLock!!.release()
            } else {
                log.warning("WiFi lock already released")
            }
        }
    }

    private val networkRequest: NetworkRequest =
        NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET).build()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            log.info("Network is available: $network")
            enable()
        }

        override fun onLost(network: Network) {    //when Wifi 【turns off】
            super.onLost(network)
            log.info("Network is lost: $network")
            disable()
        }
    }

    private val hotSpotReceiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context, intent: Intent) {
            val action = intent.action
            if ("android.net.wifi.WIFI_AP_STATE_CHANGED" == action) {
                val state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0)
                if (WifiManager.WIFI_STATE_ENABLED == state % 10) {
                    log.info("Hotspot is enabled")
                    enable()
                } else {
                    log.info("Hotspot is disabled")
                    disable()
                }
            }
        }
    }

    companion object {
        private val log = Logger.getLogger(Router::class.java.name)
    }

    init {
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        context.registerReceiver(
            hotSpotReceiver,
            IntentFilter("android.net.wifi.WIFI_AP_STATE_CHANGED")
        )
    }
}