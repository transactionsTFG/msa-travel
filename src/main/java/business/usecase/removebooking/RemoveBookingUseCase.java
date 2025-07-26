package business.usecase.removebooking;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.google.gson.Gson;

import business.service.TravelService;
import business.travel.TravelDTO;
import domainevent.registry.EventHandlerRegistry;
import msa.commons.saga.SagaPhases;

@Stateless
public class RemoveBookingUseCase implements IRemoveBookingUseCase {
    private EventHandlerRegistry eventHandlerRegistry;
    private TravelService travelService;
    private Gson gson;
    
    @Override
    public boolean removeBooking(long bookingId) {
        TravelDTO travel = this.travelService.getTravelByIdsExternal(0L, bookingId);
        if (travel == null) 
            throw new RuntimeException("No travel found with booking ID: " + bookingId);
            
        if(!travel.isActive() || travel.getSagaPhases().equals(SagaPhases.STARTED))
            throw new RuntimeException("Cannot remove booking, travel is not completed: " + travel.getSagaPhases());
        
        

        return true;
    }

    @Inject
    public void setTravelService(TravelService travelService) {
        this.travelService = travelService;
    }

    @Inject
    public void setGson(Gson gson) {
        this.gson = gson;
    }

    @Inject
    public void setEventHandlerRegistry(EventHandlerRegistry eventHandlerRegistry) {
        this.eventHandlerRegistry = eventHandlerRegistry;
    }
}
