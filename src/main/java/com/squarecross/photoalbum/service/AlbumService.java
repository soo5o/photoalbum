package com.squarecross.photoalbum.service;

import com.squarecross.photoalbum.Constants;
import com.squarecross.photoalbum.domain.Album;
import com.squarecross.photoalbum.domain.Photo;
import com.squarecross.photoalbum.mapper.AlbumMapper;
import com.squarecross.photoalbum.repository.AlbumRepository;
import com.squarecross.photoalbum.repository.PhotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityNotFoundException;
import com.squarecross.photoalbum.dto.AlbumDto;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AlbumService {
    @Autowired
    private AlbumRepository albumRepository;
    @Autowired
    private PhotoRepository photoRepository;

    public AlbumDto getAlbumsByName(String albumName) { //앨범명으로 앨범 테이블을 검색하는 메서드
        Album res = albumRepository.findByAlbumName(albumName); //앨범명 겹치지 않는다고 가정
        if (res != null) {
            return AlbumMapper.convertToDto(res);
        } else {
            throw new EntityNotFoundException("해당 앨범은 존재하지 않습니다.");
        }
    }

    public AlbumDto getAlbum(Long albumId) { //Album 정보 조회하기 메서드
        Optional<Album> res = albumRepository.findById(albumId);
        if (res.isPresent()) {
            AlbumDto albumDto = AlbumMapper.convertToDto(res.get());
            albumDto.setCount(photoRepository.countByAlbum_AlbumId(albumId));
            return albumDto;
        } else {
            throw new EntityNotFoundException(String.format("앨범 아이디 %d로 조회되지 않았습니다", albumId));
        }
    }

    public AlbumDto createAlbum(AlbumDto albumDto) throws IOException {
        Album album = AlbumMapper.convertToModel(albumDto);
        this.albumRepository.save(album);
        this.createAlbumDirectories(album);
        return AlbumMapper.convertToDto(album);
    }
    private void createAlbumDirectories(Album album) throws IOException {
        Files.createDirectories(Paths.get(Constants.PATH_PREFIX + "/photos/original/" + album.getAlbumId()));
        Files.createDirectories(Paths.get(Constants.PATH_PREFIX + "/photos/thumb/" + album.getAlbumId()));
    }
    public List<AlbumDto> getAlbumList(String keyword, String sort, String orderBy) {
        List<Album> albums;
        if (Objects.equals(sort, "byName")){
            if (Objects.equals(orderBy, "desc")) {
                albums = albumRepository.findByAlbumNameContainingOrderByAlbumNameDesc(keyword);
            } else{
                albums = albumRepository.findByAlbumNameContainingOrderByAlbumNameAsc(keyword);
            }
        } else if (Objects.equals(sort, "byDate")) {
            if (Objects.equals(orderBy, "asc")){
                albums = albumRepository.findByAlbumNameContainingOrderByCreatedAtAsc(keyword);
            }
            else{
                albums = albumRepository.findByAlbumNameContainingOrderByCreatedAtDesc(keyword);
            }
        } else {
            throw new IllegalArgumentException("알 수 없는 정렬 기준입니다");
        }
        List<AlbumDto> albumDtos = AlbumMapper.convertToDtoList(albums);
        for(AlbumDto albumDto : albumDtos){
            List<Photo> top4 = photoRepository.findTop4ByAlbum_AlbumIdOrderByUploadedAtDesc(albumDto.getAlbumId());
            albumDto.setThumbUrls(top4.stream().map(Photo::getThumbUrl).map(c -> Constants.PATH_PREFIX + c).collect(Collectors.toList()));
        }
        return albumDtos;
    }
}
