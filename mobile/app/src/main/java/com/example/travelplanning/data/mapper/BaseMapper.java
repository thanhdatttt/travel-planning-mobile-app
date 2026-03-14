package com.example.travelplanning.data.mapper;

public interface BaseMapper<DTO, Model> {
    public Model mapToDomain(DTO dto);
}
