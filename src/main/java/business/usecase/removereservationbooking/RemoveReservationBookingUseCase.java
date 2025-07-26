package business.usecase.removereservationbooking;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.google.gson.Gson;

import business.service.TravelService;
import business.travel.TravelDTO;
import domainevent.registry.EventHandlerRegistry;
import msa.commons.saga.SagaPhases;

@Stateless
public class RemoveReservationBookingUseCase implements IRemoveReservationBookingUseCase {
    private EventHandlerRegistry eventHandlerRegistry;
    private TravelService travelService;
    private Gson gson;


    @Override
    public boolean removeReservationBooking(long reservationId, long bookingId) {
        TravelDTO travel = this.travelService.getTravelByIdsExternal(reservationId, bookingId);
        if (travel == null) 
            throw new RuntimeException("No travel found with reservation ID: " + reservationId + " and booking ID: " + bookingId);
        
        if(!travel.isActive() || travel.getSagaPhases().equals(SagaPhases.STARTED)) 
            throw new RuntimeException("Cannot remove reservation, travel is not completed: " + travel.getSagaPhases());

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
