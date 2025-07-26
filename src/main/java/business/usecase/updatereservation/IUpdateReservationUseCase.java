package business.usecase.updatereservation;

import business.dto.UpdateReservationDTO;

public interface IUpdateReservationUseCase {
    boolean updateReservation(UpdateReservationDTO reservationDTO);   
}
