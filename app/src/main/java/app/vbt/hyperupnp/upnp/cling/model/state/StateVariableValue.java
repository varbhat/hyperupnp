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

package app.vbt.hyperupnp.upnp.cling.model.state;

import app.vbt.hyperupnp.upnp.cling.model.VariableValue;
import app.vbt.hyperupnp.upnp.cling.model.meta.Service;
import app.vbt.hyperupnp.upnp.cling.model.meta.StateVariable;
import app.vbt.hyperupnp.upnp.cling.model.types.InvalidValueException;

/**
 * Represents the value of a state variable.
 *
 * @author Christian Bauer
 */
public class StateVariableValue<S extends Service> extends VariableValue {

    private final StateVariable<S> stateVariable;

    public StateVariableValue(StateVariable<S> stateVariable, Object value) throws InvalidValueException {
        super(stateVariable.getTypeDetails().getDatatype(), value);
        this.stateVariable = stateVariable;
    }

    public StateVariable<S> getStateVariable() {
        return stateVariable;
    }

}