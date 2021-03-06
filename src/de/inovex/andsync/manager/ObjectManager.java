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

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import de.inovex.andsync.Constants;
import de.inovex.andsync.exception.MissingIdException;
import de.inovex.andsync.util.DBUtil;
import de.inovex.andsync.util.Log;
import de.inovex.andsync.util.TimeUtil;
import java.util.List;
import org.bson.types.ObjectId;
import static de.inovex.andsync.Constants.*;

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
	
	private static final String META_COLLECTION = "-metainformation";
	private static final String META_DELETION_KEY = "dtime";

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
		object.put(MONGO_LAST_MODIFIED, TimeUtil.getTimestamp());
		DBCollection col = DatabaseManager.get().getCollection(collection);
		col.save(object);
		
		PushManager.get().notifyAll(collection);
		
	}
	
	public List<DBObject> findAll(String collection) {
		
		DBCollection col = DatabaseManager.get().getCollection(collection);
		return DBUtil.collectionFromCursor(col.find());
		
	}
	
	public List<DBObject> findByTime(String collection, long mtime) {
		// TODO: Also search in referenced objects, if they have changed! IMPORTANT, breaks functionallity
		// right now, if a referenced object has been changed, the client won't get that object.
		DBCollection col = DatabaseManager.get().getCollection(collection);
		DBCursor cur = col.find(new BasicDBObject(MONGO_LAST_MODIFIED, new BasicDBObject("$gt", mtime)));
		return DBUtil.collectionFromCursor(cur);
	}
	
	public DBObject find(String collection, ObjectId id) {
		DBCollection col = DatabaseManager.get().getCollection(collection);
		return col.findOne(id);
	}
	
	public long findLastModified(String collection) {
		
		DBCollection col = DatabaseManager.get().getCollection(collection);
		DBObject res = col.group(DBUtil.getEmptyObject(), DBUtil.getEmptyObject(), new BasicDBObject("maxtime", 0),
				"function(obj,prev)	{ if(prev.maxtime < obj._mtime) prev.maxtime = obj._mtime; }");
		
		if(res == null || !res.containsField("0") 
				|| !(res.get("0") instanceof DBObject) 
				|| !((DBObject)res.get("0")).containsField("maxtime")) {
			return 0;
		}
		
		return (Long)((DBObject)res.get("0")).get("maxtime");
		
	}
	
	public void update(String collection, DBObject object) {
		
		if(!object.containsField("_id")) {
			throw new MissingIdException();
		}
		
		Log.d("Updating object in database [%s]", object.toString());
		object.put(MONGO_LAST_MODIFIED, TimeUtil.getTimestamp());
		DBCollection col = DatabaseManager.get().getCollection(collection);
		col.save(object);
		
		PushManager.get().notifyAll(collection);
		
	}
	
	public void delete(String collection, ObjectId id) {
		
		DBCollection col = DatabaseManager.get().getCollection(collection);
		col.remove(new BasicDBObject("_id", id));
		
		col = DatabaseManager.get().getCollection(META_COLLECTION);
		BasicDBObject query = new BasicDBObject(Constants.MONGO_META_CLASS, collection);
		BasicDBObject deletionObj = new BasicDBObject(Constants.MONGO_META_CLASS, collection)
				.append(META_DELETION_KEY, TimeUtil.getTimestamp());
		col.update(query, deletionObj, true, false);
		
		PushManager.get().notifyAll(collection);
		
	}
	
	public int getLastDeleted(String collection) {
		DBCollection col = DatabaseManager.get().getCollection(META_COLLECTION);
		DBObject meta = col.findOne(new BasicDBObject(Constants.MONGO_META_CLASS, collection));
		return (meta != null && meta.get(META_DELETION_KEY) != null) 
				? Integer.valueOf(meta.get(META_DELETION_KEY).toString()) : 0;
		
	}
	
}
