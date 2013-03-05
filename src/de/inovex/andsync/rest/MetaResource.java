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

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import de.inovex.andsync.Constants;
import de.inovex.andsync.db.DatabaseManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Tim Roes <tim.roes@inovex.de>
 */
@Path("/" + Constants.REST_META_PATH + "/{collection}")
public class MetaResource {
	
	@GET
	@Path("/" + Constants.REST_META_SIZE_PATH)
	@Produces(MediaType.TEXT_PLAIN)
	public Response getSize(@PathParam("collection") String collection) {

		DBCollection col = DatabaseManager.INSTANCE.getCollection(collection);
		return Response.ok(String.valueOf(col.count())).build();

	}
	
	@GET
	@Path("/" + Constants.REST_META_DELETION_PATH)
	@Produces(MediaType.TEXT_PLAIN)
	public Response getLastDeletionTime(@PathParam("collection") String collection) {
		DBCollection col = DatabaseManager.INSTANCE.getMetaCollection();
		DBObject meta = col.findOne(new BasicDBObject(Constants.MONGO_META_CLASS, collection));
		String lastDeletion = (meta != null && meta.get(Constants.MONGO_META_DELETION) != null) ?
				String.valueOf(meta.get(Constants.MONGO_META_DELETION)) : "0";
		return Response.ok(lastDeletion).build();
	}
	
}
