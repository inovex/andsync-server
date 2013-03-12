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
package de.inovex.andsync.manager;

import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import de.inovex.andsync.Config;
import de.inovex.andsync.util.Log;
import static de.inovex.andsync.Constants.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * TODO: Introduce multithreading
 * @author Tim Roes <tim.roes@inovex.de>
 */
public class PushManager {
	
	private static final String REGISTRATION_COLLECTION = "-registration";
	private static final int SEND_RETRIES = 10;
	private static final int MAX_RECEIVERS = 1000;
	
	private static PushManager instance;
	
	public static void init(Config config) {
		instance = new PushManager(config);
	}
	
	public static PushManager get() {
		return instance;
	}
	
	private List<String> registrationIds = new LinkedList<String>();
	private Config config;
	
	private PushManager(Config config) {
		this.config = config;
		// Load all registered ids.
		DBCursor idcursor = DatabaseManager.get().getCollection(REGISTRATION_COLLECTION).find();
		while(idcursor.hasNext()) {
			registrationIds.add(idcursor.next().get(MONGO_ID).toString());
		}
	}
	
	public synchronized void register(String regId) {
		if(!registrationIds.contains(regId)) {
			registrationIds.add(regId);
			DatabaseManager.get().getCollection(REGISTRATION_COLLECTION).save(
					new BasicDBObject(MONGO_ID, regId));
		}
	}
	
	public synchronized void unregister(String regId) {
		registrationIds.remove(regId);
		DatabaseManager.get().getCollection(REGISTRATION_COLLECTION)
				.remove(new BasicDBObject(MONGO_ID, regId));
	}
	
	public synchronized void notifyAll(String collection) {
		
		if(registrationIds.size() < 1) return;
		
		if(config.getGcmKey() == null || config.getGcmKey().equals("")) {
			Log.i("GCM key is missing in configuration, so we won't send any messages.");
			return;
		}
		
		Log.i("Sending update notification to %d clients.", registrationIds.size());
		
		List<String> ids = new ArrayList<String>(registrationIds);
		
		Sender sender = new Sender(config.getGcmKey());
		Message msg = new Message.Builder().build();
		
		for(int i = 0; i < ids.size(); i += MAX_RECEIVERS) {
			try {
				MulticastResult results = sender.send(msg, ids.subList(i, 
						Math.min(i + MAX_RECEIVERS, ids.size())), SEND_RETRIES);
				// If server returned canonical ids or failures we need to look at the response
				if(results.getFailure() != 0 || results.getCanonicalIds() != 0) {
					for(int j = 0; j < results.getResults().size(); j++) {
						Result res = results.getResults().get(j);
						if(res.getMessageId() != null) {
							if(res.getCanonicalRegistrationId() != null) {
								registrationIds.set(j + i, res.getCanonicalRegistrationId());
							}
						} else {
							String error = res.getErrorCodeName();
							if(Constants.ERROR_NOT_REGISTERED.equals(error)) {
								registrationIds.remove(i + j);
							}
						}
					}
				}
			} catch(IOException ex) {
				Log.w("Could not notify clients about change in object: " + ex.getMessage());				
			}
		}
		
	}
	
}
