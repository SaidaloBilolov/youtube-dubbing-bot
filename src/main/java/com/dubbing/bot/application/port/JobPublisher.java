package com.dubbing.bot.application.port;

import com.dubbing.bot.application.dto.DubbingJob;

/**
 * Outbound port for publishing dubbing jobs to the async queue.
 * Implemented by the messaging adapter (RabbitMQ) in infrastructure.
 */
public interface JobPublisher {

    void publish(DubbingJob job);
}
