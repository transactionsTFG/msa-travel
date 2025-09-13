package business.usecase.removereservationairline;

public interface IRemoveReservationUseCase {
    boolean removeReservation(long travelId, long reservationId);
}
