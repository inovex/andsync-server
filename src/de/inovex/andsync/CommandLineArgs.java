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

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import java.util.logging.Level;

/**
 *
 * @author Tim Roes <tim.roes@inovex.de>
 */
public class CommandLineArgs {
	
	@Parameter(names = {"-h","--help"}, description = "Displays the usage of this command", help = true)
	private boolean help;
	
	@Parameter(names = {"-c","--config"}, description = "The config file to use for the server.", required = true)
	private String configFile;
	
	@Parameter(names = {"-l","--log"}, description = "How verbose the server should write logs to the "
			+ "standard output. 6 is highest verbosity, 0 is lowest. (Default: 1)", converter = LogLevelConverter.class)
	private Level logLevel;
	
	public boolean getHelp() {
		return help;
	}
	
	public String getConfigFile() {
		return configFile;
	}
	
	public Level getLogLevel() {
		return logLevel == null ? Level.WARNING : logLevel;
	}
	
	public static class LogLevelConverter implements IStringConverter<Level> {

		@Override
		public Level convert(String val) {
			try {
				int level = Integer.valueOf(val);
				switch(level) {
					case 0: 
						return Level.SEVERE;
					case 1:
						return Level.WARNING;
					case 2:
						return Level.INFO;
					case 3:
						return Level.FINE;
					case 4:
						return Level.FINER;
					case 5:
						return Level.FINEST;
					case 6:
						return Level.ALL;
					default:
						throw new Exception();
					
				}
			} catch(Exception ex) {
				throw new ParameterException("The log level value must be a numeric value between 0 and 6.");
			}
			
		}
		
	}
	
}
