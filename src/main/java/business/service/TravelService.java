package business.service;

import java.util.List;
import java.util.Map;

import business.dto.FlightHotelDTO;
import business.dto.TravelInfo;
import business.travel.TravelDTO;
import business.travel.TravelHistoryDTO;
import msa.commons.event.type.Type;

public interface TravelService {
    Map<String, FlightHotelDTO> getFlightAndHotelByParams(TravelInfo travelInfo);
    long createTravel(TravelDTO travelDTO);
    TravelDTO getTravelById(long id);
    List<TravelDTO> getTravelsByUserId(long userId);
    TravelDTO getTravelByIdsExternal(long idReservation, long idBooking);
    TravelDTO initTransaction(TravelDTO travelDTO);
    TravelDTO updateTransactionCommit(TravelDTO travelDTO, Type type, String jsonCommand);
    TravelDTO updateTravelRollback(TravelDTO travelDTO, Type type, boolean forceActive);
    TravelHistoryDTO getTravelHistoryBySagaId(String sagaId);
    void forceEndTransaction(TravelDTO travelDTO);
}
