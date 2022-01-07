package code.dan.scraping.service;

import code.dan.scraping.model.request.ExtractCSVRequest;
import code.dan.scraping.model.request.TotalDataRequest;
import code.dan.scraping.model.response.ExtractCSVResponse;
import code.dan.scraping.model.response.ProductListResponse;
import code.dan.scraping.model.response.ProductResponse;
import code.dan.scraping.util.CommonUtility;
import code.dan.scraping.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExtractToCSVService implements BaseService<ExtractCSVRequest, ExtractCSVResponse> {

    private final GetTokopediaHandphoneService getTokopediaHandphoneService;
    private final CommonUtility commonUtility;

    private static final String CSV_EXTENSION = ".csv";
    private static final String UNDERSCORE = "_";
    private static final String[] HEADER = new String[] { "Name of Product", "Description", "Image Link", "Price",
            "Rating", "Merchant Name" };

    @Override
    public ExtractCSVResponse execute(ExtractCSVRequest input) {
        log.info("ExtractToCSVService [start]");
        doValidateRequest(input);
        ProductListResponse productListResponse = getTokopediaHandphoneService
                .execute(TotalDataRequest.builder().totalData(input.getTotalData()).build());
        String filename = input.getFilename().toUpperCase() + UNDERSCORE + commonUtility.getUUID() + CSV_EXTENSION;
        String filenameResult = writeToCSV(filename, productListResponse);
        log.info("ExtractToCSVService [end]");
        return ExtractCSVResponse.builder().filenameResult(filenameResult).build();
    }

    private void doValidateRequest(ExtractCSVRequest input) {
        if (StringUtils.isEmpty(input.getFilename())) {
            log.error("Filename is empty");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Constants.ERR_MSG_INVALID_REQUEST);
        }
        if (ObjectUtils.isEmpty(input.getTotalData())) {
            log.error("Total Data is empty");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Constants.ERR_MSG_INVALID_REQUEST);
        }
    }

    private String writeToCSV(String filename, ProductListResponse productListResponse){
        try{
            File file = new File(filename);
            FileWriter fileWriter = new FileWriter(file);
            try (CSVPrinter printer = new CSVPrinter(fileWriter, CSVFormat.DEFAULT
                    .withHeader(HEADER))) {
                for (ProductResponse productResponse : productListResponse.getProductList()) {
                    printer.printRecord(productResponse.getProductName(), 
                        productResponse.getDescription(), 
                        productResponse.getImageLink(),
                        productResponse.getPrice(),
                        productResponse.getRating(),
                        productResponse.getMerchantName());
                }
            }
            return file.getAbsolutePath();
        }catch (IOException e){
            log.error("writeToCSV.error = {}",e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Constants.ERR_MSG_SOMETHING_WENT_WRONG);
        }
    }

}
