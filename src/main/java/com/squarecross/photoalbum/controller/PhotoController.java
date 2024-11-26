package com.squarecross.photoalbum.controller;

import com.squarecross.photoalbum.dto.PhotoDto;
import com.squarecross.photoalbum.service.PhotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController   //Spring 에서 관리하는 Controller 이면서, Rest API 목적으로 사용함
@RequestMapping("/albums/{albumId}/photos")  //해당 컨트롤러가 처리할 URL 경로의 앞부분
public class PhotoController {
    @Autowired
    private PhotoService photoService;
    @RequestMapping(value="/{photoId}", method = RequestMethod.GET)
    public ResponseEntity<PhotoDto> getPhotoInfo(@PathVariable("photoId") final long photoId){
        PhotoDto photo = photoService.getPhoto(photoId);
        return new ResponseEntity<>(photo, HttpStatus.OK);
    }
}
