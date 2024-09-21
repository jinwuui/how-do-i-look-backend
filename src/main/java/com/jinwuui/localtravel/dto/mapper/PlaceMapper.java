package com.jinwuui.localtravel.dto.mapper;

import java.util.List;

import com.jinwuui.localtravel.dto.request.PlaceCreateRequest;
import com.jinwuui.localtravel.dto.response.PagingResponse;
import com.jinwuui.localtravel.dto.response.PlaceResponse;
import com.jinwuui.localtravel.dto.service.PlaceDto;
import com.jinwuui.localtravel.dto.service.PlaceSimpleDto;

public class PlaceMapper {

    public static PlaceDto toPlaceDto(PlaceCreateRequest request) {
        return PlaceDto.builder()
                .name(request.getName())
                .description(request.getDescription())
                .lat(request.getLat())
                .lng(request.getLng())
                .rating(request.getRating())
                .categories(request.getCategories())
                .images(request.getImages())
                .build();
    }

    public static PagingResponse<PlaceResponse> toPagingPlaceResponse(List<PlaceSimpleDto> placeSimpleDtos) {
    List<PlaceResponse> placeResponses = placeSimpleDtos.stream()
            .map((PlaceSimpleDto dto) -> PlaceResponse.builder()
                    .placeId(dto.getPlaceId())
                    .name(dto.getName())
                    .lat(dto.getLat())
                    .lng(dto.getLng())
                    .categories(dto.getCategories())
                    .isFavorite(dto.getIsFavorite())
                    .build())
            .collect(java.util.stream.Collectors.toList());

    return PagingResponse.<PlaceResponse>builder()
            .size(placeResponses.size())
            .items(placeResponses)
            .build();
    }
}
