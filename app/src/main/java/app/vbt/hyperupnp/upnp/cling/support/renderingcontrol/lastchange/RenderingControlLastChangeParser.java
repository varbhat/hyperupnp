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

package app.vbt.hyperupnp.upnp.cling.support.renderingcontrol.lastchange;

import java.util.Set;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import app.vbt.hyperupnp.upnp.cling.model.ModelUtil;
import app.vbt.hyperupnp.upnp.cling.support.lastchange.EventedValue;
import app.vbt.hyperupnp.upnp.cling.support.lastchange.LastChangeParser;

/**
 * @author Christian Bauer
 */
public class RenderingControlLastChangeParser extends LastChangeParser {

    public static final String NAMESPACE_URI = "urn:schemas-upnp-org:metadata-1-0/RCS/";
    public static final String SCHEMA_RESOURCE = "org/fourthline/cling/support/renderingcontrol/metadata-1.0-rcs.xsd";

    @Override
    protected String getNamespace() {
        return NAMESPACE_URI;
    }

    @Override
    protected Source[] getSchemaSources() {
        // TODO: Android 2.2 has a broken SchemaFactory, we can't validate
        // http://code.google.com/p/android/issues/detail?id=9491&q=schemafactory&colspec=ID%20Type%20Status%20Owner%20Summary%20Stars
        if (!ModelUtil.ANDROID_RUNTIME) {
            return new Source[]{new StreamSource(
                    Thread.currentThread().getContextClassLoader().getResourceAsStream(SCHEMA_RESOURCE)
            )};
        }
        return null;
    }

    @Override
    protected Set<Class<? extends EventedValue>> getEventedVariables() {
        return RenderingControlVariable.ALL;
    }
}
