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
package org.openmrs.contrib.databaseexporter;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.InputStream;
import java.lang.Exception;import java.util.Properties;

public class DatabaseExporterTest {

	@Test
	public void shouldTest() throws Exception {

		InputStream is = this.getClass().getClassLoader().getResourceAsStream("org/openmrs/contrib/databaseexporter/configuration.json");
		String json = IOUtils.toString(is);
		Configuration config = Configuration.loadFromJson(json);

		DatabaseExporter.export(config);
	}
}

