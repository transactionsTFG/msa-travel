package business.service;

import business.travel.TravelDTO;
import business.travel.TravelHistoryDTO;
import msa.commons.event.type.Type;

public interface TravelService {
    long createTravel(TravelDTO travelDTO);
    TravelDTO getTravelById(long id);
    TravelDTO updateTravelCommit(TravelDTO travelDTO, Type type, String jsonCommand);
    TravelDTO updateTravelRollback(TravelDTO travelDTO, Type type);
    TravelHistoryDTO getTravelHistoryBySagaId(String sagaId);
}
