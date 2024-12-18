package com.squarecross.photoalbum.service;

import com.squarecross.photoalbum.Constants;
import com.squarecross.photoalbum.domain.Photo;
import com.squarecross.photoalbum.dto.AlbumDto;
import com.squarecross.photoalbum.mapper.AlbumMapper;
import com.squarecross.photoalbum.repository.PhotoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import com.squarecross.photoalbum.domain.Album;
import com.squarecross.photoalbum.repository.AlbumRepository;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

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
    @Test
    void testAlbumCreate() throws IOException {
        AlbumDto albumDto = new AlbumDto();
        albumDto.setAlbumName("myTests");
        AlbumDto createAlbumDto = albumService.createAlbum(albumDto);
        AlbumDto albumName = albumService.getAlbumsByName("myTests");
        assertEquals(createAlbumDto.getAlbumId(), albumName.getAlbumId());
        Album album = AlbumMapper.convertToModel(createAlbumDto);
        deleteAlbumDirectories(album);
    }
    private void deleteAlbumDirectories(Album album) throws IOException {
        deleteDirectoryRecursively(Paths.get(Constants.PATH_PREFIX + "/photos/original/" + album.getAlbumId()));
        deleteDirectoryRecursively(Paths.get(Constants.PATH_PREFIX + "/photos/thumb/" + album.getAlbumId()));
    }

    private void deleteDirectoryRecursively(Path directory) throws IOException {
        if (Files.exists(directory)) {
            Files.walk(directory)
                    .sorted(Comparator.reverseOrder()) // 하위 파일 및 디렉토리를 먼저 삭제
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to delete " + path, e);
                        }
                    });
        }
    }
    @Test
    void testAlbumRepository() throws InterruptedException {
        Album album1 = new Album();
        Album album2 = new Album();
        album1.setAlbumName("aaaa");
        album2.setAlbumName("aaab");

        albumRepository.save(album1);
        TimeUnit.SECONDS.sleep(1); //시간차를 벌리기위해 두번째 앨범 생성 1초 딜레이
        albumRepository.save(album2);

        //최신순 정렬, 두번째로 생성한 앨범이 먼저 나와야합니다
        List<Album> resDate = albumRepository.findByAlbumNameContainingOrderByCreatedAtDesc("aaa");
        assertEquals("aaab", resDate.get(0).getAlbumName()); // 0번째 Index가 두번째 앨범명 aaab 인지 체크
        assertEquals("aaaa", resDate.get(1).getAlbumName()); // 1번째 Index가 첫번째 앨범명 aaaa 인지 체크
        assertEquals(2, resDate.size()); // aaa 이름을 가진 다른 앨범이 없다는 가정하에, 검색 키워드에 해당하는 앨범 필터링 체크

        //앨범명 정렬, aaaa -> aaab 기준으로 나와야합니다
        List<Album> resName = albumRepository.findByAlbumNameContainingOrderByAlbumNameAsc("aaa");
        assertEquals("aaaa", resName.get(0).getAlbumName()); // 0번째 Index가 두번째 앨범명 aaaa 인지 체크
        assertEquals("aaab", resName.get(1).getAlbumName()); // 1번째 Index가 두번째 앨범명 aaab 인지 체크
        assertEquals(2, resName.size()); // aaa 이름을 가진 다른 앨범이 없다는 가정하에, 검색 키워드에 해당하는 앨범 필터링 체크
    }
    @Test
    void testChangeAlbumName() throws IOException {
        //앨범 생성
        AlbumDto albumDto = new AlbumDto();
        albumDto.setAlbumName("변경전");
        AlbumDto res = albumService.createAlbum(albumDto);

        Long albumId = res.getAlbumId(); // 생성된 앨범 아이디 추출
        AlbumDto updateDto = new AlbumDto();
        updateDto.setAlbumName("변경후"); // 업데이트용 Dto 생성
        albumService.changeName(albumId, updateDto);

        AlbumDto updatedDto = albumService.getAlbum(albumId);

        //앨범명 변경되었는지 확인
        assertEquals("변경후", updatedDto.getAlbumName());
    }
    @Test
    void testDeleteAlbum() throws IOException {
        //앨범 생성
        AlbumDto albumDto = new AlbumDto();
        albumDto.setAlbumName("새로운 앨범");
        AlbumDto res = albumService.createAlbum(albumDto);
        AlbumDto albumName = albumService.getAlbumsByName("새로운 앨범");
        Long albumId = res.getAlbumId(); // 생성된 앨범 아이디 추출
        assertEquals(albumId, albumName.getAlbumId());
        albumService.deleteAlbum(albumId);
        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> albumService.deleteAlbum(albumId)
        );
        assertEquals(
                String.format("Album ID '%d'가 존재하지 않습니다", albumId),
                exception.getMessage()
        );
    }
}