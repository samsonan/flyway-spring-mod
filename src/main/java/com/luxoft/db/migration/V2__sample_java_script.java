package com.luxoft.db.migration;

import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.luxoft.sample.SampleServiceBean;

/**
 * Example of a Java-based migration.
 */
@Component
public class V2__sample_java_script implements SpringJdbcMigration {
    
    private static Logger log = LoggerFactory.getLogger(V2__sample_java_script.class);
    
    private SampleServiceBean sampleBean;
    
    @Autowired
    public V2__sample_java_script(SampleServiceBean sampleBean) {
		this.sampleBean = sampleBean;
	}

	public void migrate(JdbcTemplate template) throws Exception {
    	log.debug("migration started. sampleBean: {}", sampleBean);
    	
    	for (int i=0;i<10;i++) {
    	    Thread.sleep(1000);
    	}
    	
        log.debug("migration finished");
    }
}