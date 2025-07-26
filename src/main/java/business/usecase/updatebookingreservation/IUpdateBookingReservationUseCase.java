package business.usecase.updatebookingreservation;

import business.dto.UpdateReservationBookingDTO;

public interface IUpdateBookingReservationUseCase {
    boolean updateBookingReservation(UpdateReservationBookingDTO bookingReservationDTO);
}
