package ru.practicum.stats.dto.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RecommendationEvent {
    Long eventId;
    double score;
}
