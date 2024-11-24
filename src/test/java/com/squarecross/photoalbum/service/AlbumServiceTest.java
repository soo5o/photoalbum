package com.squarecross.photoalbum.service;

import com.squarecross.photoalbum.domain.Photo;
import com.squarecross.photoalbum.dto.AlbumDto;
import com.squarecross.photoalbum.repository.PhotoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import com.squarecross.photoalbum.domain.Album;
import com.squarecross.photoalbum.repository.AlbumRepository;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AlbumServiceTest {
    @Autowired
    AlbumRepository albumRepository;
    @Autowired
    PhotoRepository photoRepository;
    @Autowired
    AlbumService albumService;

    @Test
    void getAlbum() {
        Album album = new Album();
        album.setAlbumName("테스트");
        Album savedAlbum = albumRepository.save(album);
        //방금 저장한 앨범 id로 데이터 조회되는지 확인
        AlbumDto resAlbum = albumService.getAlbum(savedAlbum.getAlbumId());
        assertEquals("테스트", resAlbum.getAlbumName());
    }
    @Test
    void getAlbum_ThrowsException_WhenAlbumIdNotFound() {
        // 존재하지 않는 앨범 ID로 조회 시도
        Long nonExistentAlbumId = 999L; // 테스트에서 사용되지 않을 가능성이 높은 ID
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> albumService.getAlbum(nonExistentAlbumId)
        );
        // 예외 메시지 검증
        assertEquals(
                String.format("앨범 아이디 %d로 조회되지 않았습니다", nonExistentAlbumId),
                exception.getMessage()
        );
    }
    @Test
    void getAlbumsByName(){
        Album album = new Album();
        album.setAlbumName("테스팅");
        Album savedAlbum = albumRepository.save(album);
        AlbumDto albumName = albumService.getAlbumsByName("테스팅"); //똑같이 일치하는 값만 필터링됨
        assertEquals(savedAlbum.getAlbumId(),albumName.getAlbumId());
    }
    @Test
    void service_ThrowsException_WhenAlbumNameNotFound() {
        String nonExistentName = "존재하지않는앨범";
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> albumService.getAlbumsByName(nonExistentName)
        );
        assertEquals(
                "해당 앨범은 존재하지 않습니다.",
                exception.getMessage()
        );
    }
    @Test
    void testPhotoCount(){
        Album album = new Album();
        album.setAlbumName("테스트");
        Album savedAlbum = albumRepository.save(album);
        //사진을 생성하고, setAlbum을 통해 앨범을 지정해준 이후, repository에 사진을 저장한다
        Photo photo1 = new Photo();
        photo1.setFileName("사진1");
        photo1.setAlbum(savedAlbum);
        photoRepository.save(photo1);
        Photo photo2 = new Photo();
        photo2.setFileName("사진2");
        photo2.setAlbum(savedAlbum);
        photoRepository.save(photo2);
        Photo photo3 = new Photo();
        photo3.setFileName("사진3");
        photo3.setAlbum(savedAlbum);
        photoRepository.save(photo3);
        assertEquals(3, albumService.getAlbum(savedAlbum.getAlbumId()).getCount());
    }
}