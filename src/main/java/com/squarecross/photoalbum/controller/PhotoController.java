package com.squarecross.photoalbum.controller;

import com.squarecross.photoalbum.dto.PhotoDto;
import com.squarecross.photoalbum.service.PhotoService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
    //사진 업로드 API
    @RequestMapping(value="", method = RequestMethod.POST)
    public ResponseEntity<List<PhotoDto>> uploadPhotos(@PathVariable("albumId") final Long albumId, @RequestParam("photos") MultipartFile[] files) {
        List<PhotoDto> photos = new ArrayList<>();
        for (MultipartFile file : files) {
            PhotoDto photoDto = photoService.savePhoto(file, albumId);
            photos.add(photoDto);
        }
        return new ResponseEntity<>(photos, HttpStatus.OK);
    }
    //사진 다운로드 API
    @RequestMapping(value="/download", method = RequestMethod.GET)
    public void downloadPhotos(@RequestParam("photoIds") Long[] photoIds, HttpServletResponse response) {
        try {
            if (photoIds.length == 1){
                File file = photoService.getImageFile(photoIds[0]);
                OutputStream outputStream = response.getOutputStream();
                IOUtils.copy(new FileInputStream(file), outputStream);
                outputStream.close();
            }
            else{
                try (ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream())) {
                    for (Long photoId : photoIds) {
                        File file = photoService.getImageFile(photoId);
                        try (FileInputStream fis = new FileInputStream(file)) {
                            // ZIP Entry 추가
                            ZipEntry zipEntry = new ZipEntry(file.getName());
                            zipOut.putNextEntry(zipEntry);
                            // 파일 데이터를 ZIP에 복사
                            IOUtils.copy(fis, zipOut);
                            zipOut.closeEntry();
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Error");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @RequestMapping(value="", method = RequestMethod.GET)
    public ResponseEntity<List<PhotoDto>> getPhotoList(@RequestParam(value="keyword", required=false, defaultValue="") final String keyword,
                                                       @RequestParam(value="sort", required=false, defaultValue = "byDate") final String sort,
                                                       @RequestParam(value="orderBy", required=false, defaultValue="") final String orderBy) {
        List<PhotoDto> photoDtos = photoService.getPhotoList(keyword, sort, orderBy);
        return new ResponseEntity<>(photoDtos, HttpStatus.OK);
    }
}
