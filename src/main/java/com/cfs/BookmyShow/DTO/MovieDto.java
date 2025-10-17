package com.cfs.BookmyShow.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovieDto {
    private Long id;
    private String title;
    private String description;
    private String language;
    private  String genre;
    private Integer durationMins;
    private String releaseDate;
    private String posterUrl;
}
