package business.usecase.removereservationbooking;

public interface IRemoveReservationBookingUseCase {
    boolean removeReservationBooking(long travelId, long reservationId, long bookingId);
}
