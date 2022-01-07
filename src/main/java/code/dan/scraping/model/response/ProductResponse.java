package code.dan.scraping.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private String productName;
    private String description;
    private String imageLink;
    private BigDecimal price;
    private Double rating;
    private String merchantName;

    private String detailedLink;
}
