package domainevent.publisher;

import msa.commons.event.EventData;
import msa.commons.event.EventId;

public interface IJMSCommandPublisher {
    void publish(EventId event, EventData data);    
}
