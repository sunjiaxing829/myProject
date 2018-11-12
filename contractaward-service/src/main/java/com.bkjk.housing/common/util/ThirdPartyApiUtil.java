package com.bkjk.housing.common.util;

import com.bkjk.platform.devtools.util.StringUtils;
import com.bkjk.platform.http.HTTPClient;
import com.bkjk.platform.http.protocol.request.*;
import com.bkjk.platform.http.protocol.response.HTTPResponse;
import com.bkjk.platform.http.protocol.status.HTTPStatusCode;
import com.bkjk.platform.logging.LoggerFactory;
import org.apache.http.NameValuePair;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.util.Iterator;
import java.util.List;

@Component
public class ThirdPartyApiUtil {
    
    private final Logger logger = LoggerFactory.getLogger(ThirdPartyApiUtil.class);
    
    @Inject
    private HTTPClient httpClient;

    public ThirdPartyApiUtil() {
    }

    public String textPost(String url, String postBody) {
        TextPost post = new TextPost(url);
        post.setAccept("application/json");
        post.setContentType("application/json");
        post.setBody(postBody);
        try {
            logger.info("Http【TextPost】body = {}, url = {}", new Object[]{postBody, url});
            HTTPResponse response = this.httpClient.execute(post);
            HTTPStatusCode statusCode = response.getStatusCode();
            String responseText = response.getResponseText();
            logger.info("Http【TextPost】body = {}, url = {}, response = {}", new Object[]{postBody, url, responseText});
            if (!statusCode.isSuccess()) {
                logger.error("Http【TextPost】 statusCode is fail! url = {}", url);
                return null;
            } else {
                return responseText;
            }
        } catch (Exception e) {
            logger.error("Http【TextPost】 fail! url = {}, e = {}", url, e);
            return null;
        }
    }

    public String get(String url) {
        Get get = new Get(url);

        try {
            logger.info("Http【GET】url = {} ", url);
            HTTPResponse response = this.httpClient.execute(get);
            HTTPStatusCode statusCode = response.getStatusCode();
            String responseText = response.getResponseText();
            logger.info("Http【GET】url = {}, response = {}", url, responseText);
            if (!statusCode.isSuccess()) {
                logger.error("Http【GET】 statusCode is fail! url = {}", url);
                return null;
            } else {
                return responseText;
            }
        } catch (Exception e) {
            logger.error("Http【GET】 fail! url = {},e = {}", url, e);
            return null;
        }
    }

    public String put(String url, String postBody) {
        TextPut put = new TextPut(url);
        if (StringUtils.hasText(postBody)) {
            put.setAccept("application/json");
            put.setContentType("application/json");
            put.setBody(postBody);
        }
        try {
            logger.info("Http【PUT】body = {}, url = {}", postBody, url);
            HTTPResponse response = this.httpClient.execute(put);
            HTTPStatusCode statusCode = response.getStatusCode();
            String responseText = response.getResponseText();
            logger.info("Http【PUT】body = {}, url = {}, response = {}", postBody, url, responseText);
            if (!statusCode.isSuccess()) {
                logger.error("Http【PUT】 statusCode is fail! url = {}", url);
                return null;
            } else {
                return responseText;
            }
        } catch (Exception e) {
            logger.error("Http【PUT】 fail! url = {},e = {}", url, e);
            return null;
        }
    }

    public String delete(String url) {
        Delete delete = new Delete(url);

        try {
            logger.info("Http【DELETE】url = {}", url);
            HTTPResponse response = this.httpClient.execute(delete);
            HTTPStatusCode statusCode = response.getStatusCode();
            String responseText = response.getResponseText();
            logger.info("Http【DELETE】url = {}, response = {}", url, responseText);
            if (!statusCode.isSuccess()) {
                logger.error("Http【DELETE】 statusCode is fail! url = {}", url);
                return null;
            } else {
                return responseText;
            }
        } catch (Exception e) {
            logger.error("Http【DELETE】 fail! url = {},e = {}", url, e);
            return null;
        }
    }

    public String formPost(String url, List<NameValuePair> params) {
        FormPost post = new FormPost(url);
        post.setAccept("application/json");
        if (!CollectionUtils.isEmpty(params)) {
            Iterator var = params.iterator();
            while (var.hasNext()) {
                NameValuePair nvp = (NameValuePair) var.next();
                post.setParameter(nvp.getName(), nvp.getValue());
            }
        }
        try {
            logger.info("Http【formPost】body = {}, url = {}", params.toString(), url);
            HTTPResponse response = this.httpClient.execute(post);
            HTTPStatusCode statusCode = response.getStatusCode();
            String responseText = response.getResponseText();
            logger.info("Http【formPost】body = {}, url = {}, responseText ={}", params.toString(), url, responseText);
            if (!statusCode.isSuccess()) {
                logger.error("Http【formPost】 statusCode is fail! url = {}", url);
                return null;
            } else {
                return responseText;
            }
        } catch (Exception e) {
            logger.error("Http【formPost】 fail! url = {},e = {}", url, e);
            return null;
        }
    }

}
