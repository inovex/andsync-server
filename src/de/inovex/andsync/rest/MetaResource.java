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
package de.inovex.andsync.rest;

import com.mongodb.DBCollection;
import de.inovex.andsync.Constants;
import de.inovex.andsync.manager.DatabaseManager;
import de.inovex.andsync.manager.ObjectManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * This REST interface handles all meta request about objects, e.g. returning the size of a collection
 * or return the last deletion time for a collection.
 * 
 * @author Tim Roes <tim.roes@inovex.de>
 */
@Path("/" + Constants.REST_META_PATH + "/{collection}")
public class MetaResource {
	
	
	
	@GET
	@Path("/" + Constants.REST_META_SIZE_PATH)
	@Produces(MediaType.TEXT_PLAIN)
	public Response getSize(@PathParam("collection") String collection) {

		DBCollection col = DatabaseManager.get().getCollection(collection);
		return Response.ok(String.valueOf(col.count())).build();

	}
	
	@GET
	@Path("/" + Constants.REST_META_DELETION_PATH)
	@Produces(MediaType.TEXT_PLAIN)
	public Response getLastDeletionTime(@PathParam("collection") String collection) {
		int lastDeleted = ObjectManager.INSTANCE.getLastDeleted(collection);
		return Response.ok(String.valueOf(lastDeleted)).build();
	}
	
}