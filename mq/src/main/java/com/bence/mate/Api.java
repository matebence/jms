package com.bence.mate;

import javax.enterprise.context.ApplicationScoped;
import javax.annotation.security.PermitAll;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;
import javax.ws.rs.GET;

import com.bence.mate.models.Health;
import javax.ws.rs.core.Application;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("")
@ApplicationScoped
@ApplicationPath("/api")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class Api extends Application {

    private Boolean ready = Boolean.FALSE;

    public Api() {
        ready = Boolean.TRUE;
    }

    @GET
    @PermitAll
    @Path("/health")
    public Response getHealth() {
        if (!ready) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(new Health(Health.DOWN))
                    .build();
        }
        return Response.ok()
                .entity(new Health(Health.UP))
                .build();
    }
}