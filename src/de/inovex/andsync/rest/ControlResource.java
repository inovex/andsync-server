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

import de.inovex.andsync.Constants;
import de.inovex.andsync.manager.PushManager;
import de.inovex.andsync.util.Log;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

/**
 * This REST interface is responsible for registration and deregistration of devices for push 
 * notifications about changes in the database.
 * 
 * @author Tim Roes <tim.roes@inovex.de>
 */
@Path("/" + Constants.REST_CONTROL_PATH)
public class ControlResource {
	
	@PUT
	@Path("/{id}")
	public Response register(@PathParam("id") String id) {
		Log.i("Client registration request for id %s.", id);		
		PushManager.get().register(id);
		return Response.ok().build();
	}
	
	@DELETE
	@Path("/{id}")
	public Response unregister(@PathParam("id") String id) {
		Log.i("Client unregister request for id %s.", id);
		PushManager.get().unregister(id);
		return Response.ok().build();
	}
	
}
