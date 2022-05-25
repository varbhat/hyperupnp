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

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import app.vbt.hyperupnp.upnp.cling.UpnpService
import app.vbt.hyperupnp.upnp.cling.UpnpServiceConfiguration
import app.vbt.hyperupnp.upnp.cling.UpnpServiceImpl
import app.vbt.hyperupnp.upnp.cling.controlpoint.ControlPoint
import app.vbt.hyperupnp.upnp.cling.protocol.ProtocolFactory
import app.vbt.hyperupnp.upnp.cling.registry.Registry
import app.vbt.hyperupnp.upnp.cling.transport.Router

/**
 * Provides a UPnP stack with Android configuration as an application service component.
 *
 *
 * Sends a search for all UPnP devices on instantiation. See the
 * [app.vbt.hyperupnp.upnp.cling.android.AndroidUpnpService] interface for a usage example.
 *
 *
 *
 * Override the [.createRouter]
 * and [.createConfiguration] methods to customize the service.
 *
 * @author Christian Bauer
 * @author Var Bhat
 */
open class AndroidUpnpServiceImpl : Service() {
    protected lateinit var upnpService: UpnpService
    protected var binder: Binder = Binder()

    /**
     * Starts the UPnP service.
     */
    override fun onCreate() {
        super.onCreate()
        upnpService = object : UpnpServiceImpl(createConfiguration()) {
            override fun createRouter(
                protocolFactory: ProtocolFactory,
                registry: Registry
            ): Router {
                return this@AndroidUpnpServiceImpl.createRouter(
                    getConfiguration(),
                    protocolFactory,
                    this@AndroidUpnpServiceImpl
                )
            }

            @Synchronized
            override fun shutdown() {
                // First have to remove the receiver, so Android won't complain about it leaking
                // when the main UI thread exits.
                (getRouter() as AndroidRouter).unregisterNetworkCallbacks()

                // Now we can concurrently run the Cling shutdown code, without occupying the
                // Android main UI thread. This will complete probably after the main UI thread
                // is done.
                super.shutdown(true)
            }
        }
    }

    protected fun createConfiguration(): UpnpServiceConfiguration {
        return AndroidUpnpServiceConfiguration()
    }

    protected fun createRouter(
        configuration: UpnpServiceConfiguration?,
        protocolFactory: ProtocolFactory?,
        context: Context
    ): AndroidRouter {
        return AndroidRouter(configuration, protocolFactory, context)
    }

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    /**
     * Stops the UPnP service, when the last Activity unbinds from this Service.
     */
    override fun onDestroy() {
        upnpService.shutdown()
        super.onDestroy()
    }

    protected inner class Binder : android.os.Binder(), AndroidUpnpService {
        override fun get(): UpnpService {
            return upnpService
        }

        override val configuration: UpnpServiceConfiguration
            get() = upnpService.configuration
        override val registry: Registry
            get() = upnpService.registry
        override val controlPoint: ControlPoint
            get() = upnpService.controlPoint
    }
}