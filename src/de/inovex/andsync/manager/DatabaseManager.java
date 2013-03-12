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
package de.inovex.andsync.manager;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import de.inovex.andsync.Config;

/**
 *
 * @author Tim Roes <tim.roes@inovex.de>
 */
public class DatabaseManager {
	
	private static DatabaseManager instance;
	
	/**
	 * Must be called with the used {@link Config} object to initialize the {@link DatabaseManager}.
	 * Before this is called {@link #get()} will only return {@code null}.
	 * 
	 * @param config The current configuration.
	 */
	public static void init(Config config) {
		instance = new DatabaseManager(config);
	}
	
	/**
	 * Returns the {@link DatabaseManager}.
	 * 
	 * @return Database manager.
	 */
	public static DatabaseManager get() {
		return instance;
	}
	
	private DB db;
	
	private DatabaseManager(Config config) {
		try {
			Mongo mongo = new Mongo(config.getMongoHost(), config.getMongoPort());
			db = mongo.getDB(config.getMongoDb());
		} catch (Exception ex) {
			throw new Error("Could not connect to database.", ex);
		}		
	}
	
	public DBCollection getCollection(String collection) {
		return db.getCollection(collection);
	}
	
}