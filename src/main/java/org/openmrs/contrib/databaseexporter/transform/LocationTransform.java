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

import org.openmrs.contrib.databaseexporter.ColumnValue;
import org.openmrs.contrib.databaseexporter.ExportContext;
import org.openmrs.contrib.databaseexporter.TableRow;
import org.openmrs.contrib.databaseexporter.util.Util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This transform:
 *  # Replaces any address data in the location table based on the configured address configuration file
 *  # Changes the name and description of the location table
 *  # Optionally scrambles the locations associated with data such as obs, encounter, patient_program, and person_attribute
 *   in a way that these are consistent for each patient
 */
public class LocationTransform extends StructuredAddressTransform {

	//***** PROPERTIES *****

	private String nameReplacement = "${address1} Health Center";
	private String descriptionReplacement = "A de-identified health center located at ${address1}";
	private boolean scrambleLocationsInData = true;

	//***** CONSTRUCTORS *****

	public LocationTransform() {}

	@Override
	public boolean canTransform(String tableName, ExportContext context) {
		if (tableName.equals("location")) {
			return true;
		}
		if (scrambleLocationsInData) {
			for (String tableAndColumn : getLocationForeignKeys(context)) {
				String[] split = tableAndColumn.split("\\.");
				if (tableName.equals(split[0])) {
					return true;
				}
			}
		}
		return false;
	}

	//***** INSTANCE METHODS *****

	public boolean transformRow(TableRow row, ExportContext context) {
		if (row.getTableName().equals("location")) {

			Integer locationId = Integer.valueOf(row.getRawValue("location_id").toString());

			// If we are keeping a location, give it a de-identified address, name, and description
			Map<String, String> newAddress = getReplacementAddressFromIndex(locationId, context);
			for (String column : addressColumns) {
				row.setRawValue(column, newAddress.get(column));
			}
			String name = Util.evaluateExpression(nameReplacement, row).toString();
			if (usedNames.contains(name)) {
				int i = 1;
				while (usedNames.contains(name + " " + i)) {
					i++;
				}
				name = name + " " + i;
			}
			row.setRawValue("name", name);
			row.setRawValue("description", Util.evaluateExpression(descriptionReplacement, row));
			usedNames.add(name);
		}

		// If we have indicated to scramble locations then do this
		if (scrambleLocationsInData) {
			for (String tableAndColumn : getLocationForeignKeys(context)) {
				String[] split = tableAndColumn.split("\\.");
				if (row.getTableName().equals(split[0])) {
					Object currentValue = row.getRawValue(split[1]);
					if (currentValue != null) {
						ColumnValue pIdColVal = row.getColumnValueMap().get("patient_id");
						if (pIdColVal == null) {
							pIdColVal = row.getColumnValueMap().get("person_id");
						}
						String pId = (pIdColVal == null ? "" : pIdColVal.getValue().toString());
						String cacheKey = pId + ":" + row.getRawValue(split[1]);
						Integer value = patientLocationCache.get(cacheKey);
						if (value == null) {
							value = Util.getRandomElementFromList(getReplacementLocations(context));

							if (tableAndColumn.equals("location.parent_location")) {
								if (currentValue.equals(value) && getReplacementLocations(context).size() > 1) {
									while (currentValue.equals(value)) {
										value = Util.getRandomElementFromList(getReplacementLocations(context));
									}
								}
							}

							patientLocationCache.put(cacheKey, value);
						}
						if (split[0].equals("person_attribute")) {
							if (getLocationAttributeTypes(context).contains(row.getRawValue("person_attribute_type_id"))) {
								row.setRawValue(split[1], value);
							}
						}
						else {
							row.setRawValue(split[1], value);
						}
					}
				}
			}
		}

		return true;
	}

	//***** INTERNAL CACHES *****

	private Set<String> usedNames = new HashSet<String>();
	protected Set<String> getUsedNames() {
		return usedNames;
	}
	private Map<String, Integer> patientLocationCache = new HashMap<String, Integer>();

	private List<Integer> replacementLocations;
	private List<Integer> getReplacementLocations(ExportContext context) {
		if (replacementLocations == null) {
			replacementLocations = context.getTemporaryTableValues("location", "location_id");
		}
		return replacementLocations;
	}

	private List<String> locationForeignKeys;
	public List<String> getLocationForeignKeys(ExportContext context) {
		if (locationForeignKeys == null) {
			locationForeignKeys = context.getTableMetadata("location").getForeignKeys("location_id");
			locationForeignKeys.add("person_attribute.value");
		}
		return locationForeignKeys;
	}

	private Set<Integer> locationAttributeTypes;
	public Set<Integer> getLocationAttributeTypes(ExportContext context) {
		if (locationAttributeTypes == null) {
			StringBuilder q = new StringBuilder();
			q.append("select person_attribute_type_id ");
			q.append("from   person_attribute_type ");
			q.append("where  format = 'org.openmrs.Location'");
			locationAttributeTypes = context.executeIdQuery(q.toString());
		}
		return locationAttributeTypes;
	}

	//***** PROPERTY ACCESS *****

	public String getNameReplacement() {
		return nameReplacement;
	}

	public void setNameReplacement(String nameReplacement) {
		this.nameReplacement = nameReplacement;
	}

	public String getDescriptionReplacement() {
		return descriptionReplacement;
	}

	public void setDescriptionReplacement(String descriptionReplacement) {
		this.descriptionReplacement = descriptionReplacement;
	}

	public boolean isScrambleLocationsInData() {
		return scrambleLocationsInData;
	}

	public void setScrambleLocationsInData(boolean scrambleLocationsInData) {
		this.scrambleLocationsInData = scrambleLocationsInData;
	}
}
