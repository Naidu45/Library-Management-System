package com.vdn.repository;

import com.vdn.model.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GenreRepository extends JpaRepository<Genre, Long> {

    List<Genre> findByActiveTrueOrderByDisplayOrderAsc();

    List<Genre> findByParentGenreIsNullAndActiveTrueOrderByDisplayOrderAsc();

    List<Genre> findByParentGenreIdAndActiveTrueOrderByDisplayOrderAsc(Long ParentGenreId);

    long countByActiveTrue();

//    @Query("select count(b) from book b where b.genre.id=:genreId")
//    long countBooksByGenre(@Param("genreId") Long genreId);
}
