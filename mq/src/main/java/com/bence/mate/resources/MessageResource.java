package com.bence.mate.resources;

import com.bence.mate.services.MessagingService;
import com.bence.mate.models.Message;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.POST;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RequestScoped
@Path("/messages")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MessageResource {

    @Inject
    private MessagingService messagingService;

    @POST
    @Path("/send")
    @RolesAllowed("user")
    public Response sendMessage(Message message) {
        messagingService.queue(message);
        return Response.status(Response.Status.CREATED)
                .entity(message).build();
    }

    @POST
    @Path("/broadcast")
    @RolesAllowed("admin")
    public Response broadcastMessage(Message message) {
        messagingService.topic(message);
        return Response.status(Response.Status.CREATED)
                .entity(message).build();
    }
}
