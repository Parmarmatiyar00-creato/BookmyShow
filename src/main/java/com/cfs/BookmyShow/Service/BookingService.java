package com.cfs.BookmyShow.Service;

import com.cfs.BookmyShow.DTO.*;
import com.cfs.BookmyShow.Exception.ResourceNotFoundException;
import com.cfs.BookmyShow.Exception.SeatUnavailableException;
import com.cfs.BookmyShow.Repo.BookingRepo;
import com.cfs.BookmyShow.Repo.ShowRepo;
import com.cfs.BookmyShow.Repo.ShowSeatRepo;
import com.cfs.BookmyShow.Repo.UserRepo;
import com.cfs.BookmyShow.model.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @Transactional      //Annotation to consistent ACID properties
    public BookingDto createBooking(BookingReqDto bookingRequest){
        User user = userRepo.findById(bookingRequest.getUserId())
                .orElseThrow(()->new ResourceNotFoundException("User not Found"));

        Show show = showRepo.findById(bookingRequest.getShowId())
                .orElseThrow(()->new ResourceNotFoundException("Show not Found"));


        List<ShowSeat> selectedSeats = showSeatRepo.findAllById(bookingRequest.getSeatsId());
        for(ShowSeat seat: selectedSeats){
            if(!"AVAILABLE".equals((seat.getStatus()))){
                throw  new SeatUnavailableException("Seat " + seat.getSeat().getSeatNumber() + " isn't available");
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
        return mapToBookingDto(saveBooking, selectedSeats);
    }

    public BookingDto getBookingById(Long id){
        Booking booking = bookingRepo.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Booking not found by given ID"));
        List<ShowSeat> seats = showSeatRepo.findAll()
                .stream().filter(seat->seat.getBooking()!=null && seat.getBooking().getId().equals(booking.getBookingId()))
                .collect(Collectors.toList());
        return mapToBookingDto(booking, seats);
    }

    public List<BookingDto> getBookingByUserId(Long userId){
        List<Booking> booking = bookingRepo.findByUserId(userId);
        return booking.stream()
                .map(booking1->{
                    List<ShowSeat> seats = showSeatRepo.findAll()
                            .stream()
                            .filter(seat->seat.getBooking()!=null && seat.getBooking().getId().equals(booking1.getId()))
                            .collect(Collectors.toList());
                    return mapToBookingDto(booking1, seats);
                })
                .collect(Collectors.toList());
    }

    public BookingDto getBookingByNumber(String bookingNumber){
        Booking booking = bookingRepo.findByBookingNum(bookingNumber)
                .orElseThrow(()->new ResourceNotFoundException("Booking not found by given ID"));
        List<ShowSeat> seats = showSeatRepo.findAll()
                .stream().filter(seat->seat.getBooking()!=null && seat.getBooking().getId().equals(booking.getBookingId()))
                .collect(Collectors.toList());
        return mapToBookingDto(booking, seats);
    }

    @Transactional
    public BookingDto cancelBooking(Long id){
        Booking booking = bookingRepo.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Booking Not found by Id"));
        booking.setStatus("CANCELLED");
        List<ShowSeat> seats = showSeatRepo.findAll()
                .stream()
                .filter(seat->seat.getBooking()!=null && seat.getBooking().getId().equals(booking.getBookingId()))
                .collect(Collectors.toList());

        seats.forEach(seat->{
            seat.setStatus("AVAILABLE");
            seat.setBooking(null);
        });
        if(booking.getPayment()!=null){
            booking.getPayment().setStatus("REFUNDED");
        }
        Booking updateBooking = bookingRepo.save(booking);
        showSeatRepo.saveAll(seats);
        return mapToBookingDto(updateBooking, seats);
    }

    private BookingDto mapToBookingDto(Booking booking, List<ShowSeat> seats){
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(booking.getId());
        bookingDto.setBookingNumber(booking.getBookingId());
        bookingDto.setBookingTime(booking.getBookingTime());
        bookingDto.setStatus(booking.getStatus());
        bookingDto.setTotalAmount(bookingDto.getTotalAmount());

        //user mapping
        UserDto userDto = new UserDto();
        userDto.setId(booking.getUser().getId());
        userDto.setName(booking.getUser().getName());
        userDto.setEmail(booking.getUser().getEmail());
        userDto.setPhoneNumber(booking.getUser().getPhoneNumber());
        bookingDto.setUser(userDto);

        //show mapping
        ShowDto showDto = new ShowDto();
        showDto.setId(bookingDto.getShow().getId());
        showDto.setStartTime(booking.getShow().getStartTime());
        showDto.setEndTime(booking.getShow().getEndTime());

        //movie mapping
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
        theaterDto.setId(bookingDto.getShow().getScreen().getTheater().getId());
        theaterDto.setName(bookingDto.getShow().getScreen().getTheater().getName());
        theaterDto.setAddress(bookingDto.getShow().getScreen().getTheater().getAddress());
        theaterDto.setCity(bookingDto.getShow().getScreen().getTheater().getCity());
        theaterDto.setTotalScreens(bookingDto.getShow().getScreen().getTheater().getTotalScreens());

        screenDto.setTheater(theaterDto);
        showDto.setScreen(screenDto);
        bookingDto.setShow(showDto);

        List<ShowSeatDto>seatDtos = seats.stream()
                .map(seat->{
                    ShowSeatDto seatDto = new ShowSeatDto();
                    seatDto.setId(seat.getId());
                    seatDto.setStatus(seat.getStatus());
                    seatDto.setPrice(seat.getPrice());

                    SeatDto seatDto1 = new SeatDto();
                    seatDto1.setId(seat.getSeat().getId());
                    seatDto1.setSeatNumber(seat.getSeat().getSeatNumber());
                    seatDto1.setSeatType(seat.getSeat().getSeatType());
                    seatDto1.setBasePrice(seat.getSeat().getBasePrice());
                    seatDto.setSeat(seatDto1);
                    return seatDto;
                }).collect(Collectors.toList());
        bookingDto.setSeats(seatDtos);

        if(booking.getPayment()!= null){
            PaymentDto paymentDto = new PaymentDto();
            paymentDto.setId(bookingDto.getPayment().getId());
            paymentDto.setAmount(bookingDto.getPayment().getAmount());
            paymentDto.setPaymentMethod(bookingDto.getPayment().getPaymentMethod());
            paymentDto.setPaymentTime(bookingDto.getPayment().getPaymentTime());
            paymentDto.setStatus(bookingDto.getPayment().getStatus());
            paymentDto.setTransactionId(bookingDto.getPayment().getTransactionId());
            bookingDto.setPayment(paymentDto);
        }
        return bookingDto;
    }
}
