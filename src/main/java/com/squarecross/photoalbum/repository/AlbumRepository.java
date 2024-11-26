package com.squarecross.photoalbum.repository;

import com.squarecross.photoalbum.domain.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {
    Album findByAlbumName(String name); //쿼리 메서드
    List<Album> findByAlbumNameContainingOrderByCreatedAtDesc(String keyword);
    List<Album> findByAlbumNameContainingOrderByCreatedAtAsc(String keyword);

    List<Album> findByAlbumNameContainingOrderByAlbumNameAsc(String keyword);
    List<Album> findByAlbumNameContainingOrderByAlbumNameDesc(String keyword);
}
