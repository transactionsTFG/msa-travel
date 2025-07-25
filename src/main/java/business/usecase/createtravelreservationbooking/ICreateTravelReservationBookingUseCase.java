package business.usecase.createtravelreservationbooking;

import msa.commons.controller.agency.reservationbooking.CreateAirlineAndHotelReservationDTO;

public interface ICreateTravelReservationBookingUseCase {
    boolean createTravelReservationBooking(CreateAirlineAndHotelReservationDTO dto);
}
