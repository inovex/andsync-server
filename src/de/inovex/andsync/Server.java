/*
 * Copyright 2012 Tim Roes <tim.roes@inovex.de>.
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

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.net.httpserver.HttpServer;
import de.inovex.andsync.manager.DatabaseManager;
import de.inovex.andsync.manager.PushManager;
import java.io.File;
import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import org.ini4j.Ini;

/**
 *
 * @author Tim Roes <tim.roes@inovex.de>
 */
public class Server {
	
	public static final int EXIT_CODE_OK = 0;
	public static final int EXIT_CODE_WRONG_ARGS = 1;
	public static final int EXIT_CODE_NO_CONFIG = 2;
	public static final int EXIT_CODE_WRONG_CONFIG = 3;
	public static final int EXIT_CODE_SERVER_STARTUP = 4;
	
	public static void main(String[] args) {
		
		CommandLineArgs params = new CommandLineArgs();
		
		JCommander parser = new JCommander(params);
		parser.setProgramName("andsync");
		
		// Parse command line arguments
		try {
			parser.parse(args);
		} catch(ParameterException ex) {
			// Print error message and usage information if command line arguments couldn't be parsed.
			System.err.println(ex.getMessage());
			parser.usage();
			System.exit(EXIT_CODE_WRONG_ARGS);
		}
		
		// Show help and exit if requested
		if(params.getHelp()) {
			parser.usage();
			System.exit(EXIT_CODE_OK);
		}
		
		// Check if config file exists and is openable
		File configFile = new File(params.getConfigFile());
		if(!configFile.isFile() || !configFile.canRead()) {
			System.err.println("The given config file path doesn't exist or isn't readable.");
			System.exit(EXIT_CODE_NO_CONFIG);
		}
		
		// Try to parse ini file to configuration
		Config cfg = null;
		try {
			Ini ini = new Ini(configFile);
			cfg = new Config(ini);
		} catch(Exception ex) {
			System.err.println("Error parsing the config file:\n" + ex.getMessage());
			System.exit(EXIT_CODE_WRONG_CONFIG);
		}
		
		// Set log level according to argument
		Level logLevel = params.getLogLevel();
		
		Logger log = LogManager.getLogManager().getLogger("");
		for(Handler h : log.getHandlers()) {
			h.setLevel(logLevel);
		}

		
		try {
			DatabaseManager.init(cfg);
			PushManager.init(cfg);
			
			new Server(cfg).run();
		} catch(IOException ex) {
			System.err.println("Could not start server:\n" + ex.getMessage());
			System.exit(EXIT_CODE_SERVER_STARTUP);
		}
		
	}
	
	private HttpServer server;
	private Config config;
	
	private Server(Config config) throws IOException {
		this.config = config;
		server = createServer();
	}
	
	private HttpServer createServer() throws IOException {
		return HttpServerFactory.create(String.format("http://127.0.0.1:%d/", config.getPort()));
	}
	
	public void run() {
		server.start();
	}
	
}
