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
package de.inovex.andsync.rest;

import com.mongodb.DBObject;
import de.inovex.andsync.Constants;
import de.inovex.andsync.db.ObjectManager;
import de.inovex.andsync.exception.MissingIdException;
import de.inovex.andsync.util.BsonConverter;
import de.inovex.andsync.util.Log;
import java.util.List;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.bson.types.ObjectId;

/**
 * This is the main REST interface, that will handle all the client requests,
 * related to object management.
 * 
 * @author Tim Roes <tim.roes@inovex.de>
 */
@Path("/" + Constants.REST_OBJECT_PATH + "/{collection}")
@Produces(MediaType.APPLICATION_OCTET_STREAM)
public class ObjectResource {
	
	/**
	 * Will be called for any {@code PUT} request. A {@code PUT} is used, if the
	 * client created a new object, that isn't known to the server yet. The server
	 * will store that object with help of the {@link ObjectManager#save(java.lang.String, com.mongodb.DBObject)} 
	 * method.
	 * 
	 * @param collection The name of the collection in which to store the object in.
	 * @param object The BSON representation as a byte array of the new object.
	 * @return The response, that should be send back to the client. For a {@code PUT}
	 *		request, this response cannot hold any content.
	 */
	@PUT
	public Response putObject(@PathParam("collection") String collection, byte[] object) {
		
		Log.d("Received PUT from client [data.length=%s,collection=%s]", object.length, collection);
		
		List<DBObject> dbobjects = BsonConverter.fromBsonList(object);
		
		if(dbobjects == null) {
			// If there was no object in content, return BAD_REQUEST error.
			return Response.status(Status.BAD_REQUEST).build();
		}
		
		for(DBObject dbo : dbobjects) {
			ObjectManager.INSTANCE.save(collection, dbo);
		}

		return Response.ok().build();
		
	}
	
	@GET
	public Response getObjects(@PathParam("collection") String collection) {
		
		// TODO: split into several chunks if too many objects
		Log.d("Received GET from client [collection=%s]", collection);
		
		List<DBObject> objects = ObjectManager.INSTANCE.findAll(collection);
		
		return Response.ok(BsonConverter.toBSON(objects)).build();
			
	}
	
	@GET
	@Path("/{id}")
	public Response getById(@PathParam("collection") String collection, @PathParam("id") String id) {
		
		Log.d("Received GET from client with id [collection=%s,id=%s]", collection, id);
		
		DBObject obj = ObjectManager.INSTANCE.find(collection, id);
		
		return Response.ok(BsonConverter.toBSON(obj)).build();
		
	}
	
	@DELETE
	@Path("/{id}")
	public Response deleteObjects(@PathParam("collection") String collection, @PathParam("id") String id) {
		
		Log.d("Received DELETE from client [collection=%s,id=%s]", collection, id);
		
		ObjectManager.INSTANCE.delete(collection, new ObjectId(id));
		
		return Response.ok().build();
		
	}
	
	@POST
	public Response postObject(@PathParam("collection") String collection, byte[] object) {
		
		Log.d("Received POST from client [data.length=%s,collection=%s]", object.length, collection);
		
		List<DBObject> dbobjects = BsonConverter.fromBsonList(object);
		
		if(dbobjects == null) {
			// If there was no object in content, return BAD_REQUEST error.
			return Response.status(Status.BAD_REQUEST).build();
		}
		
		try {
			for(DBObject dbo : dbobjects) {
				ObjectManager.INSTANCE.update(collection, dbo);
			}			
		} catch(MissingIdException ex) {
			return Response.status(Status.BAD_REQUEST).entity("Missing ID").build();
		}
		
		return Response.ok().build();
		
	}

}