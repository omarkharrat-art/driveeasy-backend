package com.driveeasy.location.client;

import com.driveeasy.location.exception.BusinessException;
import com.driveeasy.location.exception.ConflictException;
import com.driveeasy.location.exception.ErrorResponse;
import com.driveeasy.location.exception.ResourceNotFoundException;
import com.driveeasy.location.exception.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientResponseException;

/**
 * Turns an error received from another microservice into OUR exceptions,
 * so the caller gets a meaningful code:
 *   remote 404 -> 404, remote 409 -> 409, remote 400 -> 400,
 *   service down / timeout / 5xx -> 503
 */
final class RemoteErrorTranslator {

    private static final Logger log = LoggerFactory.getLogger(RemoteErrorTranslator.class);

    private RemoteErrorTranslator() {
    }

    static RuntimeException translate(String service, RuntimeException ex) {
        if (ex instanceof RestClientResponseException http) {
            String message = remoteMessage(http);
            return switch (http.getStatusCode().value()) {
                case 404 -> new ResourceNotFoundException(message);
                case 409 -> new ConflictException(message);
                case 400 -> new BusinessException(message);
                default -> {
                    log.warn("{} a repondu {} : {}", service, http.getStatusCode(), message);
                    yield new ServiceUnavailableException(service + " a rencontre une erreur");
                }
            };
        }
        // Connection refused, timeout, no instance in Eureka...
        log.warn("{} injoignable : {}", service, ex.getMessage());
        return new ServiceUnavailableException(service + " est indisponible");
    }

    /** Reads the "message" field of the remote ErrorResponse, if any. */
    private static String messageOrDefault(ErrorResponse body, RestClientResponseException http) {
        return (body != null && body.message() != null) ? body.message() : http.getStatusText();
    }

    private static String remoteMessage(RestClientResponseException http) {
        try {
            return messageOrDefault(http.getResponseBodyAs(ErrorResponse.class), http);
        } catch (RuntimeException parseError) {
            return http.getStatusText();
        }
    }
}
