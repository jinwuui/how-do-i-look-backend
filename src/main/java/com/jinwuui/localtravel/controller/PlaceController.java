package com.jinwuui.localtravel.controller;

import com.jinwuui.localtravel.config.UserPrincipal;
import com.jinwuui.localtravel.dto.request.PlaceCreateRequest;
import com.jinwuui.localtravel.dto.response.PagingResponse;
import com.jinwuui.localtravel.dto.response.PlaceResponse;
import com.jinwuui.localtravel.service.PlaceCreationService;
import com.jinwuui.localtravel.service.PlaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.jinwuui.localtravel.dto.mapper.PlaceMapper.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    private final PlaceCreationService placeCreationService;

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public Long post(@AuthenticationPrincipal UserPrincipal userPrincipal, @ModelAttribute @Valid PlaceCreateRequest placeCreateRequest) {
        return placeCreationService.createPlaceWithImages(userPrincipal.getUserId(), toPlaceDto(placeCreateRequest));
    }

    @GetMapping()
    public PagingResponse<PlaceResponse> getList(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal != null) {
            return toPagingPlaceResponse(placeService.readForUser(userPrincipal.getUserId()));
        } else {
            return toPagingPlaceResponse(placeService.readForAnonymous());
        }
    }

    @GetMapping(params = "category")
    public PagingResponse<PlaceResponse> getListByCategory(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestParam String category) {
        if (userPrincipal != null) {
            return toPagingPlaceResponse(placeService.readForUserByCategory(userPrincipal.getUserId(), category));
        } else {
            return toPagingPlaceResponse(placeService.readForAnonymousByCategory(category));
        }
    }

    @GetMapping("/{placeId}")
    public String get(@PathVariable String placeId) {
        return "hi im place";
    }
}
