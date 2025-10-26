package com.cfs.BookmyShow.Controller;

import com.cfs.BookmyShow.DTO.BookingDto;
import com.cfs.BookmyShow.DTO.BookingReqDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @PostMapping
    public ResponseEntity<BookingDto> createBooking(@Valid @RequestBody BookingReqDto bookingRequest){
        return new ResponseEntity<>(bookingService.createBooking(bookingRequest), HttpStatus.CREATED);
    }
}
