package com.luxoft.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ContextStartupListener {

    private static Logger log = LoggerFactory.getLogger(ContextStartupListener.class);

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.debug("context event: {}", event);
    }

}
