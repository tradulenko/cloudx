package dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImageMetaData {
    @JsonAlias("created_at")
    private BigDecimal createdAt;

    private String id;

    @JsonAlias("last_modified")
    private BigDecimal lastModified;

    @JsonAlias("object_key")
    private String objectKey;

    @JsonAlias("object_size")
    private BigDecimal objectSize;

    @JsonAlias("object_type")
    private String objectType;
}