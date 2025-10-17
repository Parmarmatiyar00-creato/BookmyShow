package com.cfs.BookmyShow.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShowSeatDto {
    private Long id;
    private SeatDto seat;
    private String status;
    private Double price;
}
