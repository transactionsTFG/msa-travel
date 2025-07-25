package business.usecase.createbookinghotel;

import msa.commons.controller.hotel.booking.CreateHotelBookingDTO;

public interface CreateBookingHotelUseCase {
    boolean createHotelBooking(CreateHotelBookingDTO dto);
}
