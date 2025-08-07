package business.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;

import business.dto.FlightHotelDTO;
import business.dto.TravelInfo;
import business.external.flight.FlightApiClient;
import business.external.flight.FlightDTO;
import business.external.flight.FlightParamsDTO;
import business.external.room.RoomApiClient;
import business.external.room.RoomInfoDTO;
import business.travel.Travel;
import business.travel.TravelDTO;
import business.travel.TravelHistoryCommits;
import business.travel.TravelHistoryDTO;
import msa.commons.saga.SagaPhases;

import msa.commons.event.type.Type;

@Stateless
public class TravelServiceImpl implements TravelService {
    private EntityManager entityManager;
    private FlightApiClient flightApiClient;
    private RoomApiClient roomApiClient;

    @Override
    public long createTravel(TravelDTO travelDTO) {
        Travel t = new Travel(travelDTO);
        TravelHistoryCommits history = new TravelHistoryCommits();
        history.setTravel(t);
        history.setSagaId(t.getSagaId());
        history.setRollbackAirline(false);
        history.setRollbackHotel(false);
        entityManager.persist(t);
        entityManager.persist(history);
        entityManager.flush();
        return t.getId(); 
    } 

    @Override
    public Map<String, FlightHotelDTO> getFlightAndHotelByParams(TravelInfo travelInfo) {
        List<RoomInfoDTO> roomList = this.roomApiClient.getRoomByParams(travelInfo.getCountryDestination(), travelInfo.getHotelName()); 
        List<FlightDTO> flightList = this.flightApiClient.getFlightsByParams(new FlightParamsDTO(
            travelInfo.getCountryOrigin(),
            travelInfo.getCountryDestination(),
            travelInfo.getCityOrigin(),
            travelInfo.getCityDestination(),
            travelInfo.getDateOrigin()
        )); 

        Map<String, List<RoomInfoDTO>> hotelsByCountry = roomList.stream()
            .collect(Collectors.groupingBy(RoomInfoDTO::getCountry));

       Map<String, List<FlightDTO>> flightsByCountry = flightList.stream()
                                                                    .collect(Collectors.groupingBy(FlightDTO::getCountryDestination));

       return flightsByCountry.entrySet().stream()
                                .filter(entry -> hotelsByCountry.containsKey(entry.getKey())) 
                                .collect(
                                    Collectors.toMap(Map.Entry::getKey, entry -> new FlightHotelDTO(entry.getValue(), hotelsByCountry.get(entry.getKey())))
                                );                                             
    }
    
    @Inject
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public TravelDTO getTravelById(long id) {
        Travel t = this.entityManager.find(Travel.class, id, LockModeType.OPTIMISTIC);
        return t.toDTO();
    }

    @Override
    public TravelDTO updateTransactionCommit(TravelDTO travelDTO, Type type, String jsonCommand) {
        Travel t = this.entityManager.find(Travel.class, travelDTO.getId(), LockModeType.OPTIMISTIC);
        if (t == null) 
            return null;
        
        t.setUserId(travelDTO.getUserId());
        t.setFlightReservationID(travelDTO.getFlightReservationID());
        t.setFlightCost(travelDTO.getFlightCost());
        t.setStatusSaga(travelDTO.getSagaPhases());
        t.setActive(travelDTO.isActive());
        t.setDate(travelDTO.getDate());
        t.setReturnDate(travelDTO.getReturnDate());
        t.setCost(travelDTO.getFlightCost() + travelDTO.getHotelCost());
        t.setHotelCost(travelDTO.getHotelCost());
        t.setHotelReservationID(travelDTO.getHotelReservationID());
        t.setPassengerCounter(travelDTO.getPassengerCounter());
        t.setTransactionActive(t.getTransactionActive() - 1);
        if (t.getTransactionActive() == 0) {
            t.setStatus(travelDTO.getStatus());
            t.setActive(travelDTO.isActive());
            t.setStatusSaga(SagaPhases.COMPLETED);
        }
        TravelHistoryCommits history = this.entityManager.find(TravelHistoryCommits.class, travelDTO.getSagaId(), LockModeType.OPTIMISTIC);
        if (type == Type.HOTEL) 
            history.setJsonCommandHotel(jsonCommand);

        if (type == Type.AIRLINE) 
            history.setJsonCommandAirline(jsonCommand);
        
        this.entityManager.merge(t);
        this.entityManager.merge(history);
        return t.toDTO();
    }

    @Override
    public TravelDTO updateTravelRollback(TravelDTO travelDTO, Type type) {
        Travel t = this.entityManager.find(Travel.class, travelDTO.getId(), LockModeType.OPTIMISTIC);
        if (t == null) 
            return null;
        
        TravelHistoryCommits history = this.entityManager.find(TravelHistoryCommits.class, travelDTO.getSagaId(), LockModeType.OPTIMISTIC);
        
        if (type == Type.HOTEL) {
            history.setRollbackHotel(true);
            t.setHotelReservationID(0L);
        }

        if (type == Type.AIRLINE) {
            history.setRollbackAirline(true);
            t.setFlightReservationID(0L);
        }

        t.setTransactionActive(0);
        t.setStatus("CANCELADO");
        t.setStatusSaga(SagaPhases.CANCELLED);
        return t.toDTO();
    }

    @Override
    public TravelHistoryDTO getTravelHistoryBySagaId(String sagaId) {
        TravelHistoryCommits th = this.entityManager.find(TravelHistoryCommits.class, sagaId, LockModeType.OPTIMISTIC);
        if (th == null)
            return null;
        return th.toDTO();
    }

    @Override
    public TravelDTO getTravelByIdsExternal(long idReservation, long idBooking) {
        Travel t = this.entityManager.createNamedQuery("Travel.findByFlightAndHotelReservation", Travel.class)
            .setParameter("flightReservationID", idReservation)
            .setParameter("hotelReservationID", idBooking)
            .getSingleResult();
        return t.toDTO();
    }

    @Override
    public List<TravelDTO> getTravelsByUserId(long userId) {
        List<Travel> travels = this.entityManager.createNamedQuery("Travel.findByUserId", Travel.class)
            .setParameter("userId", userId)
            .getResultList();
        return travels.stream().map(Travel::toDTO).toList();
    }


    @Override
    public TravelDTO initTransaction(TravelDTO travelDTO) {
        Travel t = this.entityManager.find(Travel.class, travelDTO.getId(), LockModeType.OPTIMISTIC);
        if (t == null) 
            return null;

        t.setTransactionActive(travelDTO.getTransactionActive());
        t.setSagaId(travelDTO.getSagaId());
        t.setStatusSaga(travelDTO.getSagaPhases());
        t.setActive(travelDTO.isActive());
        TravelHistoryCommits history = new TravelHistoryCommits();
        history.setTravel(t);
        history.setSagaId(t.getSagaId());
        history.setRollbackAirline(false);
        history.setRollbackHotel(false);
        this.entityManager.persist(history);
        this.entityManager.merge(t);
        return t.toDTO();
    }

    @Override
    public void forceEndTransaction(TravelDTO travelDTO) {
        Travel t = this.entityManager.find(Travel.class, travelDTO.getId(), LockModeType.OPTIMISTIC);
        if (t == null) 
            return;
        
        t.setFlightCost(travelDTO.getFlightCost());
        t.setHotelCost(travelDTO.getHotelCost());
        t.setDate(travelDTO.getDate());
        t.setReturnDate(travelDTO.getReturnDate());
        t.setCost(travelDTO.getFlightCost() + travelDTO.getHotelCost());
        t.setPassengerCounter(travelDTO.getPassengerCounter());
        this.entityManager.merge(t);
    }

    @Inject
    public void setFlightApiClient(FlightApiClient flightApiClient) {
        this.flightApiClient = flightApiClient;
    }

    @Inject
    public void setRoomApiClient(RoomApiClient roomApiClient) {
        this.roomApiClient = roomApiClient;
    }

    

}
