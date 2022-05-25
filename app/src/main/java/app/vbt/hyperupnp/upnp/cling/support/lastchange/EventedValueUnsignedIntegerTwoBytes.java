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

package app.vbt.hyperupnp.upnp.cling.support.lastchange;

import java.util.Map;

import app.vbt.hyperupnp.upnp.cling.model.types.Datatype;
import app.vbt.hyperupnp.upnp.cling.model.types.UnsignedIntegerTwoBytes;

/**
 * @author Christian Bauer
 */
public class EventedValueUnsignedIntegerTwoBytes extends EventedValue<UnsignedIntegerTwoBytes> {

    public EventedValueUnsignedIntegerTwoBytes(UnsignedIntegerTwoBytes value) {
        super(value);
    }

    public EventedValueUnsignedIntegerTwoBytes(Map.Entry<String, String>[] attributes) {
        super(attributes);
    }

    @Override
    protected Datatype getDatatype() {
        return Datatype.Builtin.UI2.getDatatype();
    }
}
