package com.cfs.BookmyShow.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingReqDto {
    private Long userId;
    private Long showId;
    private List<Long> seatsId;
    private String paymentMethod;
}
