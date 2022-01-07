package code.dan.scraping.service;

import code.dan.scraping.adaptor.TopedHpAdaptor;
import code.dan.scraping.adaptor.TopedHpDescriptionAdaptor;
import code.dan.scraping.model.request.TotalDataRequest;
import code.dan.scraping.model.response.ProductDetailResponse;
import code.dan.scraping.model.response.ProductListResponse;
import code.dan.scraping.model.response.ProductResponse;
import code.dan.scraping.util.Constants;
import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetTokopediaHandphoneService implements BaseService<TotalDataRequest, ProductListResponse>{

    @Value("${scraper.config.service.enable-debug}")
    private Boolean enableDebug;

    private final TopedHpAdaptor topedHpAdaptor;
    private final TopedHpDescriptionAdaptor topedHpDescriptionAdaptor;

    @Override
    public ProductListResponse execute(TotalDataRequest input) {
        log.info("GetTokopediaHandphoneService [start]");
        ProductListResponse productListResponse = topedHpAdaptor.execute(getTotalData(input));
        List<ProductResponse> productResponses = new ArrayList<>();
        if(ObjectUtils.isNotEmpty(productListResponse)
                && !CollectionUtils.isEmpty(productListResponse.getProductList())) {
            productResponses = productListResponse.getProductList().stream().map(productResponse -> {
                ProductDetailResponse productDetailResponse = topedHpDescriptionAdaptor.execute(productResponse.getDetailedLink());
                productResponse.setDescription(productDetailResponse.getDescription());
                productResponse.setImageLink(productDetailResponse.getImageLink());
                productResponse.setDetailedLink(null);
                return productResponse;
            }).collect(Collectors.toList());
        }
        doDebugProducts(productResponses, enableDebug);
        log.info("GetTokopediaHandphoneService [end]");
        return productListResponse;
    }

    private Integer getTotalData(TotalDataRequest input){
        if(ObjectUtils.isNotEmpty(input.getTotalData())){
            return input.getTotalData();
        }
        return Constants.DEFAULT_DATA_SIZE;
    }

    private void doDebugProducts(List<ProductResponse> productResponses, Boolean enableDebug){
        if(enableDebug){
            productResponses.forEach(data-> log.info("product json = {}", JSON.toJSONString(data)));
        }
    }

}
