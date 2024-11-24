package com.squarecross.photoalbum.controller;

import com.squarecross.photoalbum.dto.AlbumDto;
import com.squarecross.photoalbum.service.AlbumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController   //Spring 에서 관리하는 Controller 이면서, Rest API 목적으로 사용함
@RequestMapping("/albums")  //해당 컨트롤러가 처리할 URL 경로의 앞부분
public class AlbumController {
    @Autowired
    AlbumService albumService;
    @RequestMapping(value="/{albumId}", method = RequestMethod.GET)
    public ResponseEntity<AlbumDto> getAlbum(@PathVariable("albumId") final long albumId) {
        AlbumDto album = albumService.getAlbum(albumId);
        return new ResponseEntity<>(album, HttpStatus.OK);
    }
    @RequestMapping(value="", method = RequestMethod.POST)
    public ResponseEntity<AlbumDto> createAlbum(@RequestBody final AlbumDto albumDto) throws IOException{
        AlbumDto savedAlbumDto = albumService.createAlbum(albumDto);
        return new ResponseEntity<>(savedAlbumDto, HttpStatus.OK);
    }
}
/*    @RequestMapping(value="/query", method = RequestMethod.GET)
    public ResponseEntity<AlbumDto> getAlbumByQuery(@RequestParam(value="album_id") final long album_id){
        AlbumDto album = albumService.getAlbum(album_id);
        return new ResponseEntity<>(album, HttpStatus.OK);
    }
    @RequestMapping(value="/json_body", method = RequestMethod.POST)
    public ResponseEntity<AlbumDto> getAlbumByJson(@RequestBody final AlbumDto album){
        return new ResponseEntity<>(albumService.getAlbum(album.getAlbumId()), HttpStatus.OK);
    }*/