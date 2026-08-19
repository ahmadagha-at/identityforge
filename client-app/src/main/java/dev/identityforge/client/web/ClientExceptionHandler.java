package dev.identityforge.client.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ClientExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ClientExceptionHandler.class);

    @ExceptionHandler(ClientGatewayException.class)
    String gateway(ClientGatewayException exception, Model model) {
        log.warn("Downstream API request failed", exception);
        model.addAttribute("message", exception.getMessage());
        return "error";
    }

    @ExceptionHandler(Exception.class)
    String unexpected(Exception exception, Model model) {
        log.error("Unhandled client application failure", exception);
        model.addAttribute("message", "The request could not be completed.");
        return "error";
    }
}

