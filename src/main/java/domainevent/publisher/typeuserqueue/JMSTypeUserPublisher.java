package domainevent.publisher.typeuserqueue;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import javax.jms.Queue;

import domainevent.publisher.jmseventpublisher.BaseJMSEventPublisher;
import domainevent.publisher.jmseventpublisher.IEventPublisher;
import integration.producer.qualifiers.TypeUserQueue;
import msa.commons.consts.JMSQueueNames;

@Stateless
@JMSTypeUserPublisherQualifier
@Local(IEventPublisher.class)
public class JMSTypeUserPublisher extends BaseJMSEventPublisher {
    @Inject
    @Override
    public void setQueueInject(@TypeUserQueue Queue typeUserServiceQueue) {
        this.queue = typeUserServiceQueue;
    }

    @Override
    public String getQueueName() {
        return JMSQueueNames.AGENCY_TYPE_USER_SERVICE_QUEUE;
    }
}
