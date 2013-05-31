/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.contrib.databaseexporter.transform;

import org.openmrs.contrib.databaseexporter.ExportContext;
import org.openmrs.contrib.databaseexporter.TableRow;

/**
 * This transform should change the associated Concept for one or all
 * program workflow states associated with the Concept to one of another
 * Collection of Concepts.  Alternatively, it should simply scramble the
 * Concept associated with a patient_state and choose a random value out of a
 * Collection of possible values
 */
public class StateTransform extends RowTransform {

	//***** CONSTRUCTORS *****

	public StateTransform() {}

	//***** INSTANCE METHODS *****

	public boolean applyTransform(TableRow row, ExportContext context) {
		// TODO: Implement this
		return true;
	}
}