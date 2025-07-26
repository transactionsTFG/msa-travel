package business.usecase.removereservationbooking;

public interface IRemoveReservationBookingUseCase {
    boolean removeReservationBooking(long reservationId, long bookingId);
}
