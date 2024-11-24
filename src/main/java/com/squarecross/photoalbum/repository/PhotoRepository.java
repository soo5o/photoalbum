package com.squarecross.photoalbum.repository;

import com.squarecross.photoalbum.domain.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {
    //Album 내 Photo 중 입력된 파일명을 가지고있는 앨범 리스트 추출
    //List<Photo> findByPhoto_FileName(String name);
    int countByAlbum_AlbumId(Long AlbumId);
    List<Photo> findTop4ByAlbum_AlbumIdOrderByUploadedAtDesc(Long AlbumId);
}
