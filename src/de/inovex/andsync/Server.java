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

import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.net.httpserver.HttpServer;
import de.inovex.andsync.util.Log;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tim Roes <tim.roes@inovex.de>
 */
public class Server {
	
	public static void main(String[] args) throws IOException {
		Logger.getLogger("andsync").setLevel(Level.FINEST);
		new Server().run();
	}
	
	private HttpServer server;
	
	private Server() throws IOException {
		server = HttpServerFactory.create("http://127.0.0.1:4242/");
	}
	
	public void run() {
		server.start();
		Runtime.getRuntime().addShutdownHook(new ShutdownHook());
	}
	
	private class ShutdownHook extends Thread {

		@Override
		public void run() {
			Log.i("Server is shutting down.");
			server.stop(0);
		}
		
	}
	
}
