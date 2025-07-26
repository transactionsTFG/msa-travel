package business.usecase.updatebooking;

import business.dto.UpdateHotelBookingDTO;

public interface IUpdateBookingUseCase {
    public boolean updateBooking(UpdateHotelBookingDTO up);
}
