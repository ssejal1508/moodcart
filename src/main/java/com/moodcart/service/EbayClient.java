package com.moodcart.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class EbayClient {

    private static final Logger log = LoggerFactory.getLogger(EbayClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String oauthToken;
    private final String marketplaceId;

    public EbayClient(RestTemplate restTemplate,
                      @Value("${ebay.base-url}") String baseUrl,
                      @Value("${ebay.oauth-token:}") String oauthToken,
                      @Value("${ebay.marketplace-id:EBAY_US}") String marketplaceId) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.oauthToken = oauthToken;
        this.marketplaceId = marketplaceId;
    }

    public List<EbayItemSummary> searchItems(String query, int page, int size) {
        if (oauthToken == null || oauthToken.isBlank()) {
            return Collections.emptyList();
        }

        int offset = page * size;
        String url = baseUrl + "/item_summary/search?q=" + encode(query) + "&limit=" + size + "&offset=" + offset;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + normalizeOauthToken(oauthToken));
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.set("X-EBAY-C-MARKETPLACE-ID", marketplaceId);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<EbaySearchResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    EbaySearchResponse.class
            );
            EbaySearchResponse body = response.getBody();
            if (body == null || body.itemSummaries == null) {
                return Collections.emptyList();
            }
            return body.itemSummaries;
        } catch (HttpClientErrorException.Unauthorized e) {
            log.warn("eBay Browse API returned 401 (unauthorized). Your ebay.oauth-token in application.yml is invalid/expired OR includes an extra 'Bearer ' prefix.");
            return Collections.emptyList();
        } catch (HttpClientErrorException e) {
            log.warn("eBay Browse API request failed: HTTP {}", e.getStatusCode().value());
            return Collections.emptyList();
        } catch (Exception e) {
            log.debug("eBay request failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private String normalizeOauthToken(String rawToken) {
        if (rawToken == null) {
            return "";
        }
        String t = rawToken.trim();
        if (t.regionMatches(true, 0, "Bearer ", 0, "Bearer ".length())) {
            t = t.substring("Bearer ".length()).trim();
        }
        return t;
    }

    private String encode(String value) {
        try {
            return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EbaySearchResponse {
        @JsonProperty("itemSummaries")
        public List<EbayItemSummary> itemSummaries = new ArrayList<>();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EbayItemSummary {
        @JsonProperty("itemId")
        public String itemId;

        @JsonProperty("title")
        public String title;

        @JsonProperty("shortDescription")
        public String shortDescription;

        @JsonProperty("itemWebUrl")
        public String itemWebUrl;

        @JsonProperty("image")
        public EbayImage image;

        @JsonProperty("price")
        public EbayPrice price;

        @JsonProperty("categories")
        public List<EbayCategory> categories = new ArrayList<>();

        public BigDecimal getPriceValue() {
            if (price == null || price.value == null) {
                return null;
            }
            try {
                return new BigDecimal(price.value);
            } catch (NumberFormatException ex) {
                return null;
            }
        }

        public String getImageUrl() {
            return image != null ? image.imageUrl : null;
        }

        public String getTagsString() {
            if (categories == null || categories.isEmpty()) {
                return null;
            }
            List<String> labels = new ArrayList<>();
            for (EbayCategory c : categories) {
                if (c.categoryName != null && !c.categoryName.isBlank()) {
                    labels.add(c.categoryName);
                }
            }
            return String.join(",", labels);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EbayImage {
        @JsonProperty("imageUrl")
        public String imageUrl;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EbayPrice {
        @JsonProperty("value")
        public String value;

        @JsonProperty("currency")
        public String currency;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EbayCategory {
        @JsonProperty("categoryId")
        public String categoryId;

        @JsonProperty("categoryName")
        public String categoryName;
    }
}
