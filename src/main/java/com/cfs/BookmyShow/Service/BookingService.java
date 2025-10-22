package com.cfs.BookmyShow.Service;

import com.cfs.BookmyShow.DTO.*;
import com.cfs.BookmyShow.Exception.ResourceNotFoundException;
import com.cfs.BookmyShow.Exception.SeatUnavailException;
import com.cfs.BookmyShow.Repo.BookingRepo;
import com.cfs.BookmyShow.Repo.ShowRepo;
import com.cfs.BookmyShow.Repo.ShowSeatRepo;
import com.cfs.BookmyShow.Repo.UserRepo;
import com.cfs.BookmyShow.model.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ShowRepo showRepo;

    @Autowired
    private ShowSeatRepo showSeatRepo;

    @Autowired
    private BookingRepo bookingRepo;

    private ModelMapper mapper;

    public BookingDto createBooking(BookingReqDto bookingRequest){
        User user = userRepo.findById(bookingRequest.getUserId())
                .orElseThrow(()->new ResourceNotFoundException("User not Found"));

        Show show = showRepo.findById(bookingRequest.getShowId())
                .orElseThrow(()->new ResourceNotFoundException("Show not Found"));


        List<ShowSeat> selectedSeats = showSeatRepo.findAllById(bookingRequest.getSeatsId());
        for(ShowSeat seat: selectedSeats){
            if(!"AVAILABLE".equals((seat.getStatus()))){
                throw  new SeatUnavailException("Seat " + seat.getSeat().getSeatNumber() + " isn't available");
            }
            seat.setStatus("LOCKED");
        }
        showSeatRepo.saveAll(selectedSeats);
        Double totalAmount = selectedSeats.stream()
                .mapToDouble(ShowSeat::getPrice)
                .sum();


        Payment payment = new Payment();
        payment.setAmount(totalAmount);
        payment.setPaymentTime(LocalDateTime.now());
        payment.setPaymentMethod(bookingRequest.getPaymentMethod());
        payment.setStatus("SUCCESS");
        payment.setTransactionId(UUID.randomUUID().toString());

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setShow(show);
        booking.setBookingTime(LocalDateTime.now());
        booking.setStatus("CONFIRM");
        booking.setTotalAmount(totalAmount);
        booking.setBookingId(UUID.randomUUID().toString());
        booking.setPayment(payment);

        Booking saveBooking = bookingRepo.save(booking);
        selectedSeats.forEach(seat->
                {
                    seat.setStatus("BOOKED");
                    seat.setBooking(saveBooking);
                });
        showSeatRepo.saveAll(selectedSeats);
        return mapper.map()
    }

    private BookingDto mapToBookingDto(Booking booking, List<ShowSeat> seats){
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(booking.getId());
        bookingDto.setBookingNumber(booking.getBookingId());
        bookingDto.setBookingTime(booking.getBookingTime());
        bookingDto.setStatus(booking.getStatus());
        bookingDto.setTotalAmount(bookingDto.getTotalAmount());

        UserDto userDto = new UserDto();
        userDto.setId(booking.getUser().getId());
        userDto.setName(booking.getUser().getName());
        userDto.setEmail(booking.getUser().getEmail());
        userDto.setPhoneNumber(booking.getUser().getPhoneNumber());
        bookingDto.setUser(userDto);

        ShowDto showDto = new ShowDto();
        showDto.setId(bookingDto.getShow().getId());
        showDto.setStartTime(booking.getShow().getStartTime());
        showDto.setEndTime(booking.getShow().getEndTime());

        MovieDto movieDto = new MovieDto();
        movieDto.setId(booking.getShow().getMovie().getId());
        movieDto.setTitle(booking.getShow().getMovie().getTitle());
        movieDto.setDescription(booking.getShow().getMovie().getDescription());
        movieDto.setLanguage(booking.getShow().getMovie().getLanguage());
        movieDto.setGenre(booking.getShow().getMovie().getGenre());
        movieDto.setDurationMins(booking.getShow().getMovie().getDurationMins());
        movieDto.setReleaseDate(booking.getShow().getMovie().getReleaseDate());
        movieDto.setPosterUrl(booking.getShow().getMovie().getPosterUrl());

        ScreenDto screenDto = new ScreenDto();
        screenDto.setId(bookingDto.getShow().getScreen().getId());
        screenDto.setName(bookingDto.getShow().getScreen().getName());
        screenDto.setTotalSeats(bookingDto.getShow().getScreen().getTotalSeats());

        TheaterDto theaterDto = new TheaterDto();

    }
}
