package business.service;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;

import business.travel.Travel;
import business.travel.TravelDTO;

@Stateless
public class TravelServiceImpl implements TravelService {
    private EntityManager entityManager;

    @Override
    public long createTravel(TravelDTO travelDTO) {
        Travel t = new Travel(travelDTO);
        entityManager.persist(t);
        entityManager.flush();
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
    public TravelDTO updateTravel(TravelDTO travelDTO) {
        Travel t = this.entityManager.find(Travel.class, travelDTO.getId(), LockModeType.OPTIMISTIC);
        if (t == null) 
            return null;
        
        t.setUserId(travelDTO.getUserId());
        t.setFlightReservationID(travelDTO.getFlightReservationID());
        t.setFlightCost(travelDTO.getFlightCost());
        t.setStatusSaga(travelDTO.getSagaPhases());
        t.setActive(travelDTO.isActive());
        t.setStatus(travelDTO.getStatus());
        t.setCost(travelDTO.getFlightCost() + travelDTO.getHotelCost());
        t.setHotelReservationID(travelDTO.getHotelReservationID());
        t.setPassengerCounter(travelDTO.getPassengerCounter());
        this.entityManager.merge(t);
        return t.toDTO();
    }
}
