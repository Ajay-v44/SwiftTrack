package com.swifttrack.OrderService.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.swifttrack.OrderService.models.OrderQuote;
import com.swifttrack.OrderService.models.OrderQuoteSession;
import com.swifttrack.OrderService.repositories.OrderQuoteSessionRepository;
import com.swifttrack.dto.orderDto.OrderQuoteSessionResponse;

@Service
public class OrderQuoteSessionService {
    OrderQuoteSessionRepository orderQuoteSessionRepository;

    public OrderQuoteSessionService(OrderQuoteSessionRepository orderQuoteSessionRepository) {
        this.orderQuoteSessionRepository = orderQuoteSessionRepository;
    }

    public List<OrderQuoteSessionResponse> getOrderQuoteSession(UUID quoteSessionId) {
        OrderQuoteSession orderQuoteSession = orderQuoteSessionRepository
                .findActiveSessionById(quoteSessionId, LocalDateTime.now()).orElse(null);
        if (orderQuoteSession == null) {
            throw new RuntimeException("OrderQuoteSession not found");
        }
        List<OrderQuote> orderQuotes = orderQuoteSession.getQuotes();
        List<OrderQuoteSessionResponse> orderQuoteSessionResponses = orderQuotes.stream()
                .map(orderQuote -> new OrderQuoteSessionResponse(orderQuote.getProviderCode(), orderQuote.getCurrency(),
                        orderQuote.getAiScore()))
                .toList();
        return orderQuoteSessionResponses;
    }

}
