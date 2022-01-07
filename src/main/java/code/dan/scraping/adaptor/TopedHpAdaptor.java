package code.dan.scraping.adaptor;

import code.dan.scraping.model.response.ProductListResponse;
import code.dan.scraping.model.response.ProductResponse;
import code.dan.scraping.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


@Component
@Slf4j
@RequiredArgsConstructor
public class TopedHpAdaptor {

    private final String baseUrl = "https://www.tokopedia.com/p/handphone-tablet/handphone";
    private final String topAdsUrl = "https://ta.tokopedia.com/promo";
    private final String pageUrl = "?page=";

    @Value("${scraper.config.jsoup.timeout}")
    private Integer timeout;

    @Value("${scraper.config.adaptor.enable-debug}")
    private Boolean enableDebug;

    private static final String USER_AGENT = "Mozilla";
    private static final Integer UNLIMITED = 0;

    private static final String PATH_PRODUCT_LIST = "div[class].css-bk6tzz.e1nlzfl3";
    private static final String PATH_PRODUCT_NAME = "span.css-1bjwylw";

    private static final String PATH_DETAIL_LINK = "a";
    private static final String DETAIL_LINK = "abs:href";

    private static final String PATH_PRICE = "span.css-o5uqvq";
    private static final String PATH_RATING_1 = "div.css-153qjw7";
    private static final String PATH_RATING_2 = "div";
    private static final String PATH_RATING_3 = "img.css-177n1u3";
    private static final String PATH_MERCHANT_NAME = "span.css-1kr22w3";

    private static final String PREFIX_REAL_LINK = "r=";
    private static final String AND_SYMBOL = "&";

    public ProductListResponse execute(Integer totalData) {
        try{
            List<ProductResponse> productResponses = new ArrayList<>();
            for(int page = 1; productResponses.size() < totalData; page ++){
                Connection connection = Jsoup.connect(baseUrl + pageUrl + page);
                connection.timeout(timeout);
                connection.userAgent(USER_AGENT);
                connection.maxBodySize(UNLIMITED);
                Document document = connection.get();
                Elements productList = document.select(PATH_PRODUCT_LIST);
                log.info("fetch product size = {}",productList.size());
                productList.forEach(product->{
                    doDebugProduct(product, enableDebug);
                    BigDecimal price = getCleanPrice(product.select(PATH_PRICE).text());
                    Double rating = getFormattedRating(product.select(PATH_RATING_1)
                            .select(PATH_RATING_2)
                            .select(PATH_RATING_3).size());
                    String detailedLink = getLink(product.select(PATH_DETAIL_LINK).attr(DETAIL_LINK));
                    productResponses.add(ProductResponse.builder()
                            .productName(product.select(PATH_PRODUCT_NAME).text())
                            .detailedLink(detailedLink)
                            .price(price)
                            .rating(rating)
                            .merchantName(product.select(PATH_MERCHANT_NAME).get(1).text())
                            .build());
                });
            }
            return ProductListResponse.builder().productList(productResponses).build();
        }catch (Exception e){
            log.error("TopedHpAdaptor.error = {}",e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Constants.ERR_MSG_SOMETHING_WENT_WRONG);
        }
    }

    private void doDebugProduct(Element product, Boolean flag){
        if(flag){
            log.info("productName = {}", product.select(PATH_PRODUCT_NAME).text());
            log.info("detailed Link = {}", getLink(product.select(PATH_DETAIL_LINK).attr(DETAIL_LINK)));
            log.info("productPrice = {}", product.select(PATH_PRICE).text());
            log.info("rating = {}", product.select(PATH_RATING_1).select(PATH_RATING_2).select(PATH_RATING_3).size());
            log.info("merchantName = {}", product.select(PATH_MERCHANT_NAME).get(1).text());
        }
    }

    private BigDecimal getCleanPrice(String priceText){
        return new BigDecimal(priceText.replace("Rp","").replace(".",""));
    }

    private Double getFormattedRating(Integer starRatings){
        return Double.parseDouble(String.valueOf(starRatings));
    }

    private String getLink(String url) {
        if(url.contains(topAdsUrl)){
            try{
                return URLDecoder.decode(url.substring(url.indexOf(PREFIX_REAL_LINK) + 2).split(AND_SYMBOL)[0], StandardCharsets.UTF_8.name());
            }catch (UnsupportedEncodingException e){
                log.error("Parsing Exception on getting link with message = {}",e.getMessage());
                return url;
            }
        }
        return url;
    }

}
