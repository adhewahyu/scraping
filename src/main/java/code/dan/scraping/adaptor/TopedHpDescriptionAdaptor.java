package code.dan.scraping.adaptor;

import code.dan.scraping.model.response.ProductDetailResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class TopedHpDescriptionAdaptor {

    @Value("${scraper.config.jsoup.timeout}")
    private Integer timeout;

    @Value("${scraper.config.adaptor.enable-debug}")
    private Boolean enableDebug;

    private static final String USER_AGENT = "Mozilla";
    private static final Integer UNLIMITED = 0;

    private static final String PATH_DESC_1 = "div.css-1k1relq";
    private static final String PATH_DESC_2 = "span.css-o0scwi.e1iszlzh0";
    private static final String DESC = "div";

    private static final String PATH_IMAGE_LINK_1 = "div.css-1q3zvcj";
    private static final String PATH_IMAGE_LINK_2 = "div.css-cbnyzd.active";
    private static final String PATH_IMAGE_LINK_3 = "div.magnifier";
    private static final String IMAGE_LINK = "style";

    public ProductDetailResponse execute(String url){
        Connection connection = Jsoup.connect(url);
        connection.timeout(timeout);
        connection.userAgent(USER_AGENT);
        connection.maxBodySize(UNLIMITED);
        try {
            Document document = connection.get();
            String description = document.select(PATH_DESC_1)
                    .select(PATH_DESC_2)
                    .select(DESC).text();
            String imageLink = getImageLink(document.select(PATH_IMAGE_LINK_1)
                    .select(PATH_IMAGE_LINK_2)
                    .select(PATH_IMAGE_LINK_3).attr(IMAGE_LINK));
            doDebugProductDetail(description,imageLink,enableDebug);
            return ProductDetailResponse.builder()
                    .description(description)
                    .imageLink(imageLink)
                    .build();
        } catch (IOException e) {
            log.error("error = {} when accessing url = {}",e.getMessage(), url);
            return ProductDetailResponse.builder()
                    .description(StringUtils.EMPTY)
                    .imageLink(StringUtils.EMPTY)
                    .build();
        }
    }

    private void doDebugProductDetail(String description, String imageLink, Boolean enableDebug){
        if(enableDebug){
            log.info("Description = {}",description);
            log.info("ImageLInk = {}",imageLink);
        }
    }

    private String getImageLink(String link){
        if(StringUtils.isNotEmpty(link)){
            return link.substring(link.indexOf("(")+1,link.indexOf(")"));
        }
        return link;
    }

}
