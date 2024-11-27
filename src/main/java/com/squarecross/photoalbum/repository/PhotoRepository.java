package com.squarecross.photoalbum.repository;

import com.squarecross.photoalbum.domain.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {
    //Album 내 Photo 중 입력된 파일명을 가지고있는 앨범 리스트 추출
    //List<Photo> findByPhoto_FileName(String name);
    int countByAlbum_AlbumId(Long AlbumId);
    List<Photo> findTop4ByAlbum_AlbumIdOrderByUploadedAtDesc(Long AlbumId);
    Optional<Photo> findByFileNameAndAlbum_AlbumId(String photoName, Long albumId);
    List<Photo> findByFileNameContainingOrderByFileNameDesc(String keyword);
    List<Photo> findByFileNameContainingOrderByFileNameAsc(String keyword);
    List<Photo> findByFileNameContainingOrderByUploadedAtAsc(String keyword);
    List<Photo> findByFileNameContainingOrderByUploadedAtDesc(String keyword);
}
