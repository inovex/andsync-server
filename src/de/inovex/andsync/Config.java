/*
 * Copyright 2013 Tim Roes <tim.roes@inovex.de>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.inovex.andsync;

import org.ini4j.Ini;

/**
 *
 * @author Tim Roes <tim.roes@inovex.de>
 */
public class Config {
	
	private final int port;
	
	private final String gcmApiKey;
	
	private final String mongoHost;
	private final int mongoPort;
	private final String mongoDb;
	
	public Config(Ini ini) {
		port = requireIntValue(ini, "general", "port");
		gcmApiKey = getValue(ini, "push", "key", "");
		mongoHost = getValue(ini, "mongodb", "host", "localhost");
		mongoPort = getIntValue(ini, "mongodb", "port", 27017);
		mongoDb = getValue(ini, "mongodb", "db", "andsync");
	}
	
	private String getValue(Ini ini, String section, String key, String defaultValue)  {
		String val = ini.get(section, key);
		return (val == null || val.isEmpty()) ? defaultValue : val;
	}
	
	private int getIntValue(Ini ini, String section, String key, int defaultValue) {
		String val = ini.get(section, key);
		if(val == null || val.isEmpty()) {
			return defaultValue;
		}
		try {
			return Integer.valueOf(val);
		} catch(NumberFormatException ex) {
			throw new InvalidValueException(section, key, val, "int");
		}
	}
	
	private int requireIntValue(Ini ini, String section, String key) {
		String val = requireValue(ini, section, key);
		try {
			return Integer.valueOf(val);
		} catch(NumberFormatException ex) {
			throw new InvalidValueException(section, key, val, "int");
		}
	}
	
	private String requireValue(Ini ini, String section, String key) {
		String val = ini.get(section, key);
		if(val == null || val.isEmpty()) {
			throw new RequiredKeyMissingException(section, key);
		}
		return val;
	}
	
	public int getPort() {
		return port;
	}
	
	public String getGcmKey() {
		return gcmApiKey;
	}
	
	public String getMongoHost() {
		return mongoHost;
	}
	
	public int getMongoPort() {
		return mongoPort;
	}
	
	public String getMongoDb() {
		return mongoDb;
	}
	
	public class RequiredKeyMissingException extends RuntimeException {
		
		private static final long serialVersionUID = 1L;
		
		public RequiredKeyMissingException(String section, String key) {
			super(String.format("The required key '%s' in section '%s' in the config file is "
					+ "missing or empty.", key, section));
		}
		
	}
	
	public class InvalidValueException extends RuntimeException {
		
		private static final long serialVersionUID = 1L;
		
		public InvalidValueException(String section, String key, String value, String format) {
			super(String.format("The value of key '%s' in section '%s' was '%s' and couldn't be "
					+ "converted to type '%s'.", key, section, value, format));
		}
		
	}
	
}
