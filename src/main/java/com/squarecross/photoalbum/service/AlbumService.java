package com.squarecross.photoalbum.service;

import com.squarecross.photoalbum.domain.Album;
import com.squarecross.photoalbum.mapper.AlbumMapper;
import com.squarecross.photoalbum.repository.AlbumRepository;
import com.squarecross.photoalbum.repository.PhotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityNotFoundException;
import com.squarecross.photoalbum.dto.AlbumDto;
import java.util.Optional;

@Service
public class AlbumService {
    @Autowired
    private AlbumRepository albumRepository;
    @Autowired
    private PhotoRepository photoRepository;

    public AlbumDto getAlbumsByName(String albumName) { //앨범명으로 앨범 테이블을 검색하는 메서드
        Album res = albumRepository.findByAlbumName(albumName); //앨범명 겹치지 않는다고 가정
        if (res != null){
            return AlbumMapper.convertToDto(res);
        }
        else{
            throw new EntityNotFoundException("해당 앨범은 존재하지 않습니다.");
        }
    }
    public AlbumDto getAlbum(Long albumId){ //Album 정보 조회하기 메서드
        Optional<Album> res = albumRepository.findById(albumId);
        if (res.isPresent()){
            AlbumDto albumDto = AlbumMapper.convertToDto(res.get());
            albumDto.setCount(photoRepository.countByAlbum_AlbumId(albumId));
            return albumDto;
        } else {
            throw new EntityNotFoundException(String.format("앨범 아이디 %d로 조회되지 않았습니다", albumId));
        }
    }
}
