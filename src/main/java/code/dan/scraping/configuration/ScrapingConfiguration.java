package code.dan.scraping.configuration;

import code.dan.scraping.model.request.ExtractCSVRequest;
import code.dan.scraping.model.response.ExtractCSVResponse;
import code.dan.scraping.service.ExtractToCSVService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Configuration
@Slf4j
public class ScrapingConfiguration {

    @Autowired
    private ExtractToCSVService extractToCSVService;

    @EventListener(ApplicationReadyEvent.class)
    public void doScraping(){
        log.info("doScraping [start]");
        ExtractCSVResponse extractCSVResponse = extractToCSVService.execute(ExtractCSVRequest.builder().filename("coba").totalData(100).build());
        log.info("Extract CSV success save to file = {}",extractCSVResponse.getFilenameResult());
        log.info("doScraping [end]");
    }

}
