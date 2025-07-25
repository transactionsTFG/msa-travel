package controller;

import javax.ejb.EJB;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import business.usecase.createbookinghotel.CreateBookingHotelUseCase;
import business.usecase.createtravelreservationairline.CreateTravelAirlineReservationUseCase;
import business.usecase.createtravelreservationbooking.ICreateTravelReservationBookingUseCase;
import msa.commons.controller.agency.reservationairline.ReservationAirlineRequestDTO;
import msa.commons.controller.agency.reservationbooking.CreateAirlineAndHotelReservationDTO;
import msa.commons.controller.hotel.booking.CreateHotelBookingDTO;

@Path("/travel")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TravelController {

    private static final Logger LOGGER = LogManager.getLogger(TravelController.class);
    private CreateTravelAirlineReservationUseCase createTravelAirlineReservationUseCase;
    private CreateBookingHotelUseCase createBookingHotelUseCase;
    private ICreateTravelReservationBookingUseCase createTravelReservationBookingUseCase;

    @POST
    @Path("/reservation/airline")
    public Response createAirlineReservation(ReservationAirlineRequestDTO dto) {
        LOGGER.info("Iniciada reserva aerolinea: {}", dto);
        boolean result = createTravelAirlineReservationUseCase.createTravelAirlineReservation(dto);
        if (!result) {
            LOGGER.error("Error al crear la reserva de aerolinea");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al crear la reserva").build();
        }
        return Response.status(Response.Status.OK).entity("Peticion recibida").build();
    }

    @POST
    @Path("/reservation/hotel")
    public Response createHotelReservation(CreateHotelBookingDTO dto) {
        LOGGER.info("Iniciada reserva hotel: {}", dto);
        boolean result = createBookingHotelUseCase.createHotelBooking(dto);
        if (!result) {
            LOGGER.error("Error al crear la reserva de hotel");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al crear la reserva").build();
        }
        return Response.status(Response.Status.OK).entity("Peticion recibida").build();
    }

    @POST
    @Path("/reservation/hotel-airline")
    public Response createHotelAirlineReservation(CreateAirlineAndHotelReservationDTO dto) {
        LOGGER.info("Iniciada reserva hotel-aerolinea: {}", dto);
        boolean result = createTravelReservationBookingUseCase.createTravelReservationBooking(dto);
        if (!result) {
            LOGGER.error("Error al crear la reserva de hotel");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al crear la reserva").build();
        }
        return Response.status(Response.Status.OK).entity("Peticion recibida").build();
    }


    @EJB
    public void setCreateTravelAirlineReservationUseCase(CreateTravelAirlineReservationUseCase createTravelAirlineReservationUseCase) {
        this.createTravelAirlineReservationUseCase = createTravelAirlineReservationUseCase;
    }

    @EJB
    public void setCreateBookingHotelUseCase(CreateBookingHotelUseCase createBookingHotelUseCase) {
        this.createBookingHotelUseCase = createBookingHotelUseCase;
    }

    @EJB
    public void setCreateTravelReservationBookingUseCase(ICreateTravelReservationBookingUseCase createTravelReservationBookingUseCase) {
        this.createTravelReservationBookingUseCase = createTravelReservationBookingUseCase;
    }
}
