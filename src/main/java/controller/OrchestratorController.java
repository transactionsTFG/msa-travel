package controller;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import business.dto.CreateAirlineAndHotelReservationDTO;
import domainevent.registry.EventHandlerRegistry;
import msa.commons.event.EventId;

@Path("/orchestrator")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrchestratorController {
    
    private static final Logger LOGGER = LogManager.getLogger(OrchestratorController.class);
    private EventHandlerRegistry eventHandlerRegistry;

    @POST
    @Path("/create")
    public Response createAirlineAndHotelReservation(CreateAirlineAndHotelReservationDTO dto) {
        LOGGER.info("Iniciada reserva aerolinea y hotel: {}", dto);
        this.eventHandlerRegistry.getHandler(EventId.RESERVATION_AIRLINE_CREATE_RESERVATION_BEGIN_SAGA)
                .handle(dto.getReservation());
        this.eventHandlerRegistry.getHandler(EventId.BEGIN_CREATE_HOTEL_BOOKING).handle(dto.getBooking());

        return Response.status(Response.Status.OK).entity("Peticion recibida").build();
    }

}
