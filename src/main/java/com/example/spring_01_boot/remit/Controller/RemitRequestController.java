package com.example.spring_01_boot.remit.Controller;

import com.example.spring_01_boot.remit.dto.RemitRequest;
import com.example.spring_01_boot.remit.dto.RemitRequestResponse;
import com.example.spring_01_boot.remit.service.RemitRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class RemitRequestController {
    private final RemitRequestService remitRequestService;

    @PostMapping("/remit/create")
    public RemitRequestResponse createRemitRequest(@Valid @RequestBody RemitRequest remitRequest) {
        return remitRequestService.createRemitRequest(remitRequest.getRequesterId(), remitRequest.getReceiverId(), remitRequest.getAmount());
    }

    @GetMapping("/remit")
    public List<RemitRequestResponse> getRemitRequests(
            @RequestParam(required = false) String requesterId,
            @RequestParam(required = false) String receiverId) {
        return remitRequestService.getRemitRequestsByRequesterToReceiver(requesterId, receiverId);
    }
}
