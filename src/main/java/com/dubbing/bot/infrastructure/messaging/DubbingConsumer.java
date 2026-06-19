package com.dubbing.bot.infrastructure.messaging;

import com.dubbing.bot.application.dto.DubbingJob;
import com.dubbing.bot.application.service.DubbingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumer side of the Producer-Consumer model. A bounded number of listener
 * threads (see spring.rabbitmq.listener.simple.concurrency) pull jobs and run the
 * pipeline. Retries/back-off and DLQ routing are configured in application.yml.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DubbingConsumer {

    private final DubbingService dubbingService;

    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void onMessage(DubbingJob job) {
        log.info("Consuming dubbing job videoId={} userId={}", job.videoId(), job.userId());
        // Exceptions propagate so Spring AMQP applies retry/back-off, then DLQ.
        dubbingService.process(job);
    }
}
