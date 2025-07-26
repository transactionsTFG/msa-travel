package business.service;

import business.travel.Travel;
import business.travel.TravelDTO;
import business.travel.TravelHistoryDTO;
import msa.commons.event.type.Type;

public interface TravelService {
    long createTravel(TravelDTO travelDTO);
    TravelDTO getTravelById(long id);
    TravelDTO getTravelByIdsExternal(long idReservation, long idBooking);
    TravelDTO updateTransaction(TravelDTO travelDTO);
    TravelDTO updateTravelCommit(TravelDTO travelDTO, Type type, String jsonCommand);
    TravelDTO updateTravelRollback(TravelDTO travelDTO, Type type);
    TravelHistoryDTO getTravelHistoryBySagaId(String sagaId);
}
