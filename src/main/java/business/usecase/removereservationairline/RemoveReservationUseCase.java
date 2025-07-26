package business.usecase.removereservationairline;

import java.util.ArrayList;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.google.gson.Gson;
import com.tangosol.coherence.transaction.internal.operation.RemoveOperation;

import business.service.TravelService;
import business.travel.TravelDTO;
import domainevent.registry.EventHandlerRegistry;
import msa.commons.commands.removereservation.RemoveReservationCommand;
import msa.commons.event.EventData;
import msa.commons.event.EventId;
import msa.commons.event.eventoperation.reservation.DeleteReservation;
import msa.commons.saga.SagaPhases;

@Stateless
public class RemoveReservationUseCase implements IRemoveReservationUseCase {
    private EventHandlerRegistry eventHandlerRegistry;
    private TravelService travelService;
    private Gson gson;
    
    @Override
    public boolean removeReservation(long reservationId) {
        TravelDTO travel = this.travelService.getTravelByIdsExternal(reservationId, 0L);
        if (travel == null) 
            throw new RuntimeException("No travel found with reservation ID: " + reservationId);
        
        if(!travel.isActive() || travel.getSagaPhases().equals(SagaPhases.STARTED)) 
            throw new RuntimeException("Cannot remove reservation, travel is not completed: " + travel.getSagaPhases());
        
        final byte transactionActive = 1;
        final String sagaId = UUID.randomUUID().toString();
        RemoveReservationCommand command = new RemoveReservationCommand();
        command.setIdReservation(travel.getFlightReservationID());
        command.setIdTravel(travel.getId());
        command.setAllRemove(false);
        command.setListIdFlightInstance(new ArrayList<>());
        EventData eventData = new EventData(
            sagaId,
            DeleteReservation.DELETE_RESERVATION_ONLY_AIRLINE_BEGIN,
            new ArrayList<>(),
            command,
            transactionActive
        );
        travel.setTransactionActive(transactionActive);
        travel.setSagaId(sagaId);
        travel.setSagaPhases(SagaPhases.STARTED);
        this.travelService.updateTransaction(travel);
        this.eventHandlerRegistry.getHandler(EventId.REMOVE_RESERVATION_TRAVEL).handleCommand(this.gson.toJson(eventData));
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
