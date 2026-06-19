package com.dubbing.bot.infrastructure.messaging;

import com.dubbing.bot.application.dto.DubbingJob;
import com.dubbing.bot.application.port.JobPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ implementation of {@link JobPublisher}. Serializes the job to JSON and
 * publishes it to the dubbing exchange.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DubbingProducer implements JobPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publish(DubbingJob job) {
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY, job);
        log.debug("Published job to queue videoId={}", job.videoId());
    }
}
