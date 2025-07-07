package business.usecase.createtravelreservationairline;

import msa.commons.controller.agency.reservationairline.ReservationAirlineRequestDTO;

public interface CreateTravelAirlineReservationUseCase {
    public boolean createTravelAirlineReservation(ReservationAirlineRequestDTO request);
}
