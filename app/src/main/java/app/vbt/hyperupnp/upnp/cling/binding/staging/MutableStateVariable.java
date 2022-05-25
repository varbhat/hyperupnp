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

package app.vbt.hyperupnp.upnp.cling.binding.staging;

import java.util.List;

import app.vbt.hyperupnp.upnp.cling.model.meta.StateVariable;
import app.vbt.hyperupnp.upnp.cling.model.meta.StateVariableAllowedValueRange;
import app.vbt.hyperupnp.upnp.cling.model.meta.StateVariableEventDetails;
import app.vbt.hyperupnp.upnp.cling.model.meta.StateVariableTypeDetails;
import app.vbt.hyperupnp.upnp.cling.model.types.Datatype;

/**
 * @author Christian Bauer
 */
public class MutableStateVariable {

    public String name;
    public Datatype dataType;
    public String defaultValue;
    public List<String> allowedValues;
    public MutableAllowedValueRange allowedValueRange;
    public StateVariableEventDetails eventDetails;

    public StateVariable build() {
        return new StateVariable(
                name,
                new StateVariableTypeDetails(
                        dataType,
                        defaultValue,
                        allowedValues == null || allowedValues.size() == 0
                                ? null
                                : allowedValues.toArray(new String[allowedValues.size()]),
                        allowedValueRange == null
                                ? null :
                                new StateVariableAllowedValueRange(
                                        allowedValueRange.minimum,
                                        allowedValueRange.maximum,
                                        allowedValueRange.step
                                )
                ),
                eventDetails
        );
    }
}
