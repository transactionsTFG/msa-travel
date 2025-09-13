package business.usecase.removebooking;

public interface IRemoveBookingUseCase {
    boolean removeBooking(long travelId, long bookingId);
}
