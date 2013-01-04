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
package de.inovex.andsync.db;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import de.inovex.andsync.exception.MissingIdException;
import de.inovex.andsync.util.DBUtil;
import de.inovex.andsync.util.Log;
import java.util.List;
import org.bson.types.ObjectId;

/**
 * The {@code ObjectManager} is responsible for handling of objects. It will
 * store and load objects from the database and does all the conflict handling.
 * Its method should be called by the class, that receives the client requests,
 * with the already deserialized object.
 * 
 * @author Tim Roes <tim.roes@inovex.de>
 */
public enum ObjectManager {
	
	/**
	 * The single instance of the {@link ObjectManager}.
	 */
	INSTANCE;

	/**
	 * Save a new object to the specified collection. This won't check
	 * if there is already an object with the same {@code _id} and overwrite
	 * any preexisting object with the same {@code _id}. So that method should
	 * only be called for newly created objects from a client.
	 * 
	 * @param collection Name of the collection to store the object.
	 * @param object The {@code DBObject} to save to database.
	 */
	public void save(String collection, DBObject object) {
		
		Log.d("Storing object into database [%s]", object.toString());
		DBCollection col = DatabaseManager.INSTANCE.getCollection(collection);
		col.save(object);
		
	}
	
	public List<DBObject> findAll(String collection) {
		
		DBCollection col = DatabaseManager.INSTANCE.getCollection(collection);
		return DBUtil.collectionFromCursor(col.find());
		
	}
	
	public DBObject find(String collection, String id) {
		DBCollection col = DatabaseManager.INSTANCE.getCollection(collection);
		return col.findOne(new ObjectId(id));
	}
	
	public void update(String collection, DBObject object) {
		
		if(!object.containsField("_id")) {
			throw new MissingIdException();
		}
		
		Log.d("Updating object in database [%s]", object.toString());
		DBCollection col = DatabaseManager.INSTANCE.getCollection(collection);
		col.save(object);
		
	}
	
	public void delete(String collection, ObjectId id) {
		
		DBCollection col = DatabaseManager.INSTANCE.getCollection(collection);
		col.remove(new BasicDBObject("_id", id));
		
	}
	
}
