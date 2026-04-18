package com.example.travelplanning.data.mapper.review;

import com.example.travelplanning.data.mapper.BaseMapper;
import com.example.travelplanning.data.model.review.RatingStat;
import com.example.travelplanning.data.remote.review.dto.response.RatingStatResponse;
import java.util.ArrayList;
import java.util.List;

public class RatingStatMapper implements BaseMapper<RatingStatResponse, RatingStat> {

    @Override
    public RatingStat mapToDomain(RatingStatResponse dto) {
        if (dto == null) return null;

        return new RatingStat(
                "",
                dto.getRating(),
                dto.getCount()
        );
    }

    public List<RatingStat> mapToDomainList(List<RatingStatResponse> dtos) {
        if (dtos == null) return new ArrayList<>();
        List<RatingStat> list = new ArrayList<>();
        for (RatingStatResponse dto : dtos) {
            list.add(mapToDomain(dto));
        }
        return list;
    }
}