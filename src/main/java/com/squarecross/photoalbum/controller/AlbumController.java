package com.squarecross.photoalbum.controller;

import com.squarecross.photoalbum.dto.AlbumDto;
import com.squarecross.photoalbum.service.AlbumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.List;

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
    @RequestMapping(value="", method = RequestMethod.GET)
    public ResponseEntity<List<AlbumDto>> getAlbumList(@RequestParam(value="keyword", required=false, defaultValue="") final String keyword,
                 @RequestParam(value="sort", required=false, defaultValue = "byDate") final String sort,
                                                       @RequestParam(value="orderBy", required=false, defaultValue="") final String orderBy) {
        List<AlbumDto> albumDtos = albumService.getAlbumList(keyword, sort, orderBy);
        return new ResponseEntity<>(albumDtos, HttpStatus.OK);
    }
    @RequestMapping(value="/{albumId}", method = RequestMethod.PUT)
    public ResponseEntity<AlbumDto> updateAlbum(@PathVariable("albumId") final long albumId, @RequestBody final AlbumDto albumDto){
        AlbumDto res = albumService.changeName(albumId, albumDto);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }
    @RequestMapping(value="/{albumId}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteAlbum(@PathVariable("albumId") final long albumId) throws IOException {
        albumService.deleteAlbum(albumId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
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