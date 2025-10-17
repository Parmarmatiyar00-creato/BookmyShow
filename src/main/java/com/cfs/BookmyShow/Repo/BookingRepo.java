package com.cfs.BookmyShow.Repo;

import com.cfs.BookmyShow.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepo extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long id);
    Optional<Booking> findByBookingNum(String bookingNum);

    List<Booking> findByShowId(Long id);
}
