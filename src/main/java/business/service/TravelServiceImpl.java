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
}
