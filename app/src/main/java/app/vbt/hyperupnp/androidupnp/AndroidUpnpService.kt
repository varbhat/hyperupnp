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

import app.vbt.hyperupnp.upnp.cling.UpnpService
import app.vbt.hyperupnp.upnp.cling.UpnpServiceConfiguration
import app.vbt.hyperupnp.upnp.cling.controlpoint.ControlPoint
import app.vbt.hyperupnp.upnp.cling.registry.Registry

/**
 * Interface of the Android UPnP application service component.
 *
 *
 * Usage example in an Android activity:
 *
 * <pre>`AndroidUpnpService upnpService;
 *
 * ServiceConnection serviceConnection = new ServiceConnection() {
 * public void onServiceConnected(ComponentName className, IBinder service) {
 * upnpService = (AndroidUpnpService) service;
 * }
 * public void onServiceDisconnected(ComponentName className) {
 * upnpService = null;
 * }
 * };
 *
 * public void onCreate(...) {
 * ...
 * getApplicationContext().bindService(
 * new Intent(this, AndroidUpnpServiceImpl.class),
 * serviceConnection,
 * Context.BIND_AUTO_CREATE
 * );
 * }`</pre>
 *
 *
 * The default implementation requires permissions in `AndroidManifest.xml`:
 *
 * <pre>`<uses-permission android:name="android.permission.INTERNET"/>
 * <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
 * <uses-permission android:name="android.permission.CHANGE_WIFI_MULTICAST_STATE"/>
 * <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
 * <uses-permission android:name="android.permission.WAKE_LOCK"/>
`</pre> *
 *
 *
 * You also have to add the application service component:
 *
 * <pre>`<application ...>
 * ...
 * <service android:name="app.vbt.hyperupnp.android.AndroidUpnpServiceImpl"/>
 * </application>
`</pre> *
 *
 * @author Christian Bauer
 * @author Var Bhat
 */
// DOC:CLASS
interface AndroidUpnpService {
    /**
     * @return The actual main instance and interface of the UPnP service.
     */
    fun get(): UpnpService

    /**
     * @return The configuration of the UPnP service.
     */
    val configuration: UpnpServiceConfiguration

    /**
     * @return The registry of the UPnP service.
     */
    val registry: Registry

    /**
     * @return The client API of the UPnP service.
     */
    val controlPoint: ControlPoint
}