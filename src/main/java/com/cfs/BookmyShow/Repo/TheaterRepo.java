package com.cfs.BookmyShow.Repo;

import com.cfs.BookmyShow.model.Theater;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TheaterRepo extends JpaRepository<Theater, Long> {
    List<Theater> findByShowId(String city);
}
