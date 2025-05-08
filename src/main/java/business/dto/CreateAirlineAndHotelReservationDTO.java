package business.dto;

import lombok.Data;
import msa.commons.commands.hotelbooking.CreateHotelBookingCommand;
import msa.commons.controller.airline.reservation.create.ReservationRequestDTO;

@Data
public class CreateAirlineAndHotelReservationDTO {
    private ReservationRequestDTO reservation;
    private CreateHotelBookingCommand booking;
}
