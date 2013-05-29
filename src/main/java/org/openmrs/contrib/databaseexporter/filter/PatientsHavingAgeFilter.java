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
package org.openmrs.contrib.databaseexporter.filter;

import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.openmrs.contrib.databaseexporter.AgeRange;
import org.openmrs.contrib.databaseexporter.ExportContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Returns a particular number of patients in configured set of age ranges
 */
public class PatientsHavingAgeFilter extends PatientFilter {

	//***** PROPERTIES *****

	private Integer numberPerAgeRange;
	private List<AgeRange> ageRanges;

	//***** CONSTRUCTORS *****

	public PatientsHavingAgeFilter() {}

	//***** INSTANCE METHODS ******

	@Override
	public Collection<Integer> getPatientIds(ExportContext context) {
		List<Integer> ret = new ArrayList<Integer>();
		for (AgeRange ar : getAgeRanges()) {
			StringBuilder query = new StringBuilder("select p.patient_id from patient p, person n where p.patient_id = n.person_id");
			if (ar.getMinAge() != null) {
				query.append(" and datediff(CURRENT_DATE, n.birthdate)/365.25 >= " + ar.getMinAge());
			}
			if (ar.getMaxAge() != null) {
				query.append(" AND datediff(CURRENT_DATE, n.birthdate)/365.25 <= " + ar.getMaxAge());
			}
			if (numberPerAgeRange != null) {
				query.append(" limit " + numberPerAgeRange);
			}
			ret.addAll(context.executeQuery(query.toString(), new ColumnListHandler<Integer>()));
		}
		return ret;
	}

	//****** PROPERTY ACCESS *****

	public Integer getNumberPerAgeRange() {
		return numberPerAgeRange;
	}

	public void setNumberPerAgeRange(Integer numberPerAgeRange) {
		this.numberPerAgeRange = numberPerAgeRange;
	}

	public List<AgeRange> getAgeRanges() {
		if (ageRanges == null) {
			ageRanges = new ArrayList<AgeRange>();
		}
		return ageRanges;
	}

	public void setAgeRanges(List<AgeRange> ageRanges) {
		this.ageRanges = ageRanges;
	}
}
