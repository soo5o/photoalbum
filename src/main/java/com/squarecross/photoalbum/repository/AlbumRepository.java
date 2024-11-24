package com.squarecross.photoalbum.repository;

import com.squarecross.photoalbum.domain.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {
    //앨범명으로 앨범 테이블을 검색하는 메서드
    Album findByAlbumName(String name); //쿼리 메서드

}
