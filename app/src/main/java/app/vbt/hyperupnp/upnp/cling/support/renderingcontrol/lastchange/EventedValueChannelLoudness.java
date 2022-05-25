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

import java.util.Map;

import app.vbt.hyperupnp.upnp.cling.model.types.BooleanDatatype;
import app.vbt.hyperupnp.upnp.cling.model.types.Datatype;
import app.vbt.hyperupnp.upnp.cling.model.types.InvalidValueException;
import app.vbt.hyperupnp.upnp.cling.support.lastchange.EventedValue;
import app.vbt.hyperupnp.upnp.cling.support.model.Channel;
import app.vbt.hyperupnp.upnp.cling.support.shared.AbstractMap;

/**
 * @author Christian Bauer
 */
public class EventedValueChannelLoudness extends EventedValue<ChannelLoudness> {

    public EventedValueChannelLoudness(ChannelLoudness value) {
        super(value);
    }

    public EventedValueChannelLoudness(Map.Entry<String, String>[] attributes) {
        super(attributes);
    }

    @Override
    protected ChannelLoudness valueOf(Map.Entry<String, String>[] attributes) throws InvalidValueException {
        Channel channel = null;
        Boolean loudness = null;
        for (Map.Entry<String, String> attribute : attributes) {
            if (attribute.getKey().equals("channel"))
                channel = Channel.valueOf(attribute.getValue());
            if (attribute.getKey().equals("val"))
                loudness = new BooleanDatatype().valueOf(attribute.getValue());
        }
        return channel != null && loudness != null ? new ChannelLoudness(channel, loudness) : null;
    }

    @Override
    public Map.Entry<String, String>[] getAttributes() {
        return new Map.Entry[]{
                new AbstractMap.SimpleEntry<>(
                        "val",
                        new BooleanDatatype().getString(getValue().getLoudness())
                ),
                new AbstractMap.SimpleEntry<>(
                        "channel",
                        getValue().getChannel().name()
                )
        };
    }

    @Override
    public String toString() {
        return getValue().toString();
    }

    @Override
    protected Datatype getDatatype() {
        return null; // Not needed
    }
}