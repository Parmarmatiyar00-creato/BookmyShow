package com.cfs.BookmyShow.Repo;

import com.cfs.BookmyShow.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface MovieRepo extends JpaRepository<Movie, Long> {
    List<Movie> findByLanguage(String language);
    List<Movie> findByGenre(String genre);
    List<Movie> findByTitle(String title);
}
