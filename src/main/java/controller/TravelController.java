package controller;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import business.usecase.createtravelreservationairline.CreateTravelAirlineReservationUseCase;
import business.usecase.createtravelreservationairline.CreateTravelAirlineReservationUseCaseImpl;
import msa.commons.controller.agency.reservationairline.ReservationAirlineRequestDTO;

@Path("/travel")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TravelController {

    private static final Logger LOGGER = LogManager.getLogger(TravelController.class);
    private CreateTravelAirlineReservationUseCase createTravelAirlineReservationUseCase;

    @POST
    @Path("/reservation/airline")
    public Response createAirlineAndHotelReservation(ReservationAirlineRequestDTO dto) {
        LOGGER.info("Iniciada reserva aerolinea y hotel: {}", dto);
        boolean result = createTravelAirlineReservationUseCase.createTravelAirlineReservation(dto);
        if (!result) {
            LOGGER.error("Error al crear la reserva de aerolinea y hotel");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al crear la reserva").build();
        }
        return Response.status(Response.Status.OK).entity("Peticion recibida").build();
    }

    @Inject
    public void setCreateTravelAirlineReservationUseCase(CreateTravelAirlineReservationUseCase createTravelAirlineReservationUseCase) {
        this.createTravelAirlineReservationUseCase = createTravelAirlineReservationUseCase;
    }

}
