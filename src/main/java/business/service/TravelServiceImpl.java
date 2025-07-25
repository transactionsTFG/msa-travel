package business.service;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;

import business.travel.Travel;
import business.travel.TravelDTO;
import business.travel.TravelHistoryCommits;
import business.travel.TravelHistoryDTO;
import msa.commons.saga.SagaPhases;

import msa.commons.event.type.Type;

@Stateless
public class TravelServiceImpl implements TravelService {
    private EntityManager entityManager;

    @Override
    public long createTravel(TravelDTO travelDTO) {
        Travel t = new Travel(travelDTO);
        entityManager.persist(t);
        entityManager.flush();
        TravelHistoryCommits history = new TravelHistoryCommits();
        history.setTravel(t);
        history.setSagaId(t.getSagaId());
        history.setRollbackAirline(false);
        history.setRollbackHotel(false);
        return t.getId(); 
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
    public TravelDTO updateTravelCommit(TravelDTO travelDTO, Type type, String jsonCommand) {
        Travel t = this.entityManager.find(Travel.class, travelDTO.getId(), LockModeType.OPTIMISTIC);
        if (t == null) 
            return null;
        
        t.setUserId(travelDTO.getUserId());
        t.setFlightReservationID(travelDTO.getFlightReservationID());
        t.setFlightCost(travelDTO.getFlightCost());
        t.setStatusSaga(travelDTO.getSagaPhases());
        t.setActive(travelDTO.isActive());
        t.setCost(travelDTO.getFlightCost() + travelDTO.getHotelCost());
        t.setHotelReservationID(travelDTO.getHotelReservationID());
        t.setPassengerCounter(travelDTO.getPassengerCounter());
        t.setTransactionActive(t.getTransactionActive() - 1);
        if (t.getTransactionActive() == 0) {
            t.setStatus("COMPLETADO");
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
            t.setHotelReservationID(-1L);
        }

        if (type == Type.AIRLINE) {
            history.setRollbackAirline(true);
            t.setFlightReservationID(-1L);
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
}
