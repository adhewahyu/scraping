package code.dan.scraping.util;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CommonUtility {

    public String getUUID(){
        return UUID.randomUUID().toString();
    }

}
