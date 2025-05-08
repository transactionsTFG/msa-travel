package domainevent.publisher.airline;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.Queue;

import domainevent.publisher.jmseventpublisher.BaseJMSEventPublisher;
import domainevent.publisher.jmseventpublisher.IEventPublisher;
import integration.producer.qualifiers.AirlineQueue;
import msa.commons.consts.JMSQueueNames;

@Stateless
@JMSAirlineReservationPublisherQualifier
@Local(IEventPublisher.class)
public class JMSAirlineReservationPublisher extends BaseJMSEventPublisher {

    @Override
    @Inject
    public void setQueueInject(@AirlineQueue Queue queueInject) {
        this.queue = queueInject;
    }

    @Override
    public String getQueueName() {
        return JMSQueueNames.AIRLINE_RESERVATION_QUEUE;
    }

}
