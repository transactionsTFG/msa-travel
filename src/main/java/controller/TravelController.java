package controller;

import javax.ejb.EJB;
import javax.websocket.server.PathParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import business.dto.UpdateHotelBookingDTO;
import business.dto.UpdateReservationBookingDTO;
import business.dto.UpdateReservationDTO;
import business.usecase.createbookinghotel.CreateBookingHotelUseCase;
import business.usecase.createtravelreservationairline.CreateTravelAirlineReservationUseCase;
import business.usecase.createtravelreservationbooking.ICreateTravelReservationBookingUseCase;
import business.usecase.removebooking.IRemoveBookingUseCase;
import business.usecase.removereservationairline.IRemoveReservationUseCase;
import business.usecase.removereservationbooking.IRemoveReservationBookingUseCase;
import business.usecase.updatebooking.IUpdateBookingUseCase;
import business.usecase.updatebookingreservation.IUpdateBookingReservationUseCase;
import business.usecase.updatereservation.IUpdateReservationUseCase;
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

    private IUpdateBookingReservationUseCase updateBookingReservationUseCase;
    private IUpdateReservationUseCase updateReservationUseCase;
    private IUpdateBookingUseCase updateBookingUseCase;

    private IRemoveReservationUseCase removeReservationUseCase;
    private IRemoveReservationBookingUseCase removeReservationBookingUseCase;
    private IRemoveBookingUseCase removeBookingUseCase;

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

    @PUT
    @Path("/reservation/airline")
    public Response updateAirlineReservation(UpdateReservationDTO dto) {
        LOGGER.info("Actualizando reserva aerolinea: {}", dto);
        boolean result = updateReservationUseCase.updateReservation(dto);
        if (!result) {
            LOGGER.error("Error al actualizar la reserva de aerolinea");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al actualizar la reserva").build();
        }
        return Response.status(Response.Status.OK).entity("Peticion recibida").build();
    }

    @PUT
    @Path("/reservation/hotel")
    public Response updateHotelReservation(UpdateHotelBookingDTO dto) {
        LOGGER.info("Actualizando reserva hotel: {}", dto);
        boolean result = updateBookingUseCase.updateBooking(dto);
        if (!result) {
            LOGGER.error("Error al actualizar la reserva de hotel");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al actualizar la reserva").build();
        }
        return Response.status(Response.Status.OK).entity("Peticion recibida").build();
    }

    @PUT
    @Path("/reservation/hotel-airline")
    public Response updateHotelAirlineReservation(UpdateReservationBookingDTO dto) {
        LOGGER.info("Actualizando reserva hotel-aerolinea: {}", dto);
        boolean result = updateBookingReservationUseCase.updateBookingReservation(dto);
        if (!result) {
            LOGGER.error("Error al actualizar la reserva de hotel-aerolinea");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al actualizar la reserva").build();
        }
        return Response.status(Response.Status.OK).entity("Peticion recibida").build();
    }

    @DELETE
    @Path("/reservation/airline/{reservationId}")
    public Response removeAirlineReservation(@PathParam("reservationId") long reservationId) {
        LOGGER.info("Eliminando reserva aerolinea: {}", reservationId);
        boolean result = removeReservationUseCase.removeReservation(reservationId);
        if (!result) {
            LOGGER.error("Error al eliminar la reserva de aerolinea");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al eliminar la reserva").build();
        }
        return Response.status(Response.Status.OK).entity("Peticion recibida").build();
    }

    @DELETE
    @Path("/reservation/hotel/{bookingId}")
    public Response removeHotelReservation(@PathParam("bookingId") long bookingId) {
        LOGGER.info("Eliminando reserva hotel: {}", bookingId);
        boolean result = removeBookingUseCase.removeBooking(bookingId);
        if (!result) {
            LOGGER.error("Error al eliminar la reserva de hotel");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al eliminar la reserva").build();
        }
        return Response.status(Response.Status.OK).entity("Peticion recibida").build();
    }

    @DELETE
    @Path("/reservation/hotel-airline/{reservationId}/{bookingId}")
    public Response removeHotelAirlineReservation(@PathParam("reservationId") long reservationId, @PathParam("bookingId") long bookingId) {
        LOGGER.info("Eliminando reserva hotel-aerolinea: {}", reservationId);
        boolean result = removeReservationBookingUseCase.removeReservationBooking(reservationId, bookingId);
        if (!result) {
            LOGGER.error("Error al eliminar la reserva de hotel-aerolinea");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al eliminar la reserva").build();
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

    @EJB
    public void setRemoveReservationUseCase(IRemoveReservationUseCase removeReservationUseCase) {
        this.removeReservationUseCase = removeReservationUseCase;
    }

    @EJB
    public void setRemoveReservationBookingUseCase(IRemoveReservationBookingUseCase removeReservationBookingUseCase) {
        this.removeReservationBookingUseCase = removeReservationBookingUseCase;
    }

    @EJB
    public void setRemoveBookingUseCase(IRemoveBookingUseCase removeBookingUseCase) {
        this.removeBookingUseCase = removeBookingUseCase;
    }

    @EJB
    public void setUpdateBookingReservationUseCase(IUpdateBookingReservationUseCase updateBookingReservationUseCase) {
        this.updateBookingReservationUseCase = updateBookingReservationUseCase;
    }
    
    @EJB
    public void setUpdateReservationUseCase(IUpdateReservationUseCase updateReservationUseCase) {
        this.updateReservationUseCase = updateReservationUseCase;
    }

    @EJB
    public void setUpdateBookingUseCase(IUpdateBookingUseCase updateBookingUseCase) {
        this.updateBookingUseCase = updateBookingUseCase;
    }
}
