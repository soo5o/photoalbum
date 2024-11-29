package com.squarecross.photoalbum.service;

import com.squarecross.photoalbum.Constants;
import com.squarecross.photoalbum.domain.Album;
import com.squarecross.photoalbum.domain.Photo;
import com.squarecross.photoalbum.dto.AlbumDto;
import com.squarecross.photoalbum.dto.PhotoDto;
import com.squarecross.photoalbum.mapper.AlbumMapper;
import com.squarecross.photoalbum.mapper.PhotoMapper;
import com.squarecross.photoalbum.repository.AlbumRepository;
import com.squarecross.photoalbum.repository.PhotoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PhotoService {
    @Autowired
    private PhotoRepository photoRepository;
    @Autowired
    private AlbumRepository albumRepository;
    private final String original_path = Constants.PATH_PREFIX + "/photos/original";
    private final String thumb_path = Constants.PATH_PREFIX + "/photos/thumb";
    public PhotoDto getPhoto(Long photoId) {
        Optional<Photo> res = photoRepository.findById(photoId);
        if(res.isPresent()){
            return PhotoMapper.convertToDto(res.get());
        } else {
            throw new EntityNotFoundException(String.format("사진 아이디 %d로 조회되지 않았습니다", photoId));
        }
    }
    public File getImageFile(Long photoId) {
        Optional<Photo> res = photoRepository.findById(photoId);
        if(res.isEmpty()){
            throw new EntityNotFoundException(String.format("사진을 ID %d를 찾을 수 없습니다", photoId));
        }
        return new File(Constants.PATH_PREFIX + res.get().getOriginalUrl());
    }
    public PhotoDto savePhoto(MultipartFile file, Long albumId){
        Optional<Album> res = albumRepository.findById(albumId);
        if(res.isEmpty()){
            throw new EntityNotFoundException("앨범이 존재하지 않습니다");
        }
        String fileName = file.getOriginalFilename();
        String dataType = file.getContentType();
        if (!dataType.startsWith("image/")){
            throw new MultipartException("Invalid file type");
        }
        int fileSize = (int)file.getSize();
        //long은 64바이트 int는 32바이트다. int로 나타낼 수 있는 최대는 대략 2GB인데 그렇게 커질 일 없으니 int 로 변환 (굳이? ㅋㅋ 내맴)
        fileName = getNextFileName(fileName, albumId);
        saveFile(file, albumId, fileName);
        Photo photo = new Photo();
        photo.setOriginalUrl("/photos/original/" + albumId + "/" + fileName);
        photo.setThumbUrl("/photos/thumb/" + albumId + "/" + fileName);
        photo.setFileName(fileName);
        photo.setFileSize(fileSize);
        photo.setAlbum(res.get());
        Photo createdPhoto = photoRepository.save(photo);
        return PhotoMapper.convertToDto(createdPhoto);
    }
    private String getNextFileName(String fileName, Long albumId){
        String fileNameNoExt = StringUtils.stripFilenameExtension(fileName);
        String ext = StringUtils.getFilenameExtension(fileName);
        Optional<Photo> res = photoRepository.findByFileNameAndAlbum_AlbumId(fileName, albumId);

        int count = 2;
        while(res.isPresent()){
            fileName = String.format("%s (%d).%s", fileNameNoExt, count, ext);
            res = photoRepository.findByFileNameAndAlbum_AlbumId(fileName, albumId);
            count++;
        }
        return fileName;
    }
    private void saveFile(MultipartFile file, Long AlbumId, String fileName) {
        try {
            String filePath = AlbumId + "/" + fileName;
            Files.copy(file.getInputStream(), Paths.get(original_path + "/" + filePath));
            BufferedImage thumbImg = Scalr.resize(ImageIO.read(file.getInputStream()), Constants.THUMB_SIZE, Constants.THUMB_SIZE);
            File thumbFile = new File(thumb_path + "/" + filePath);
            String ext = StringUtils.getFilenameExtension(fileName);
            if (ext == null) {
                throw new IllegalArgumentException("No Extention");
            }
            ImageIO.write(thumbImg, ext, thumbFile);
        } catch (Exception e) {
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
        }
    }
    public List<PhotoDto> getPhotoList(Long albumId, String keyword, String sort, String orderBy) {
        List<Photo> photos;
        if (Objects.equals(sort, "byName")){
            if (Objects.equals(orderBy, "desc")) {
                photos = photoRepository.findByFileNameContainingAndAlbum_AlbumIdOrderByFileNameDesc(keyword, albumId);
            } else{
                photos = photoRepository.findByFileNameContainingAndAlbum_AlbumIdOrderByFileNameAsc(keyword, albumId);
            }
        } else if (Objects.equals(sort, "byDate")) {
            if (Objects.equals(orderBy, "asc")){
                photos = photoRepository.findByFileNameContainingAndAlbum_AlbumIdOrderByUploadedAtAsc(keyword, albumId);
            }
            else{
                photos = photoRepository.findByFileNameContainingAndAlbum_AlbumIdOrderByUploadedAtDesc(keyword, albumId);
            }
        } else {
            throw new IllegalArgumentException("알 수 없는 정렬 기준입니다");
        }
        List<PhotoDto> photoDtos = PhotoMapper.convertToDtoList(photos);
        return photoDtos;
    }
    public PhotoDto changePhoto(Long photoId, PhotoDto photoDto){
        Optional<Photo> photo = photoRepository.findById(photoId);
        if (photo.isEmpty()){
            throw new NoSuchElementException(String.format("Photo ID '%d'가 존재하지 않습니다", photoId));
        }
        Photo updatePhoto = photo.get();
        moveFile(updatePhoto.getAlbum().getAlbumId(), photoDto.getAlbumId(),updatePhoto.getFileName());
        Optional<Album> res = albumRepository.findById(photoDto.getAlbumId());
        updatePhoto.setAlbum(res.get());
        Photo changedAlbum = photoRepository.save(updatePhoto);
        return PhotoMapper.convertToDto(changedAlbum);
    }
    private void moveFile(Long albumId, Long targetAlbumId, String fileName){
        try {
            String filePath = albumId + "/" + fileName;
            String targetFilePath = targetAlbumId + "/" + fileName;
            Files.move(Paths.get(original_path + "/" + filePath), Paths.get(original_path + "/" + targetFilePath), StandardCopyOption.REPLACE_EXISTING);
            Files.move(Paths.get(thumb_path + "/" + filePath), Paths.get(thumb_path + "/" + targetFilePath), StandardCopyOption.REPLACE_EXISTING);

        } catch (Exception e) {
            throw new RuntimeException("Could not move the file. Error: " + e.getMessage());
        }
    }
    public void deletePhoto(Long albumId, Long photoId) {
        Optional<Photo> photo = photoRepository.findById(photoId);
        if (photo.isEmpty()){
            throw new NoSuchElementException(String.format("Photo ID '%d'가 존재하지 않습니다", photoId));
        }
        Photo deletePhoto = photo.get();
        photoRepository.deleteById(photoId);
        try {
            String filePath = albumId + "/" + deletePhoto.getFileName();
            Files.deleteIfExists(Paths.get(original_path + "/" + filePath));
            Files.deleteIfExists(Paths.get(thumb_path + "/" + filePath));
        } catch (IOException e) {
            throw new RuntimeException("Could not delete the file. Error: " + e.getMessage());
        }
    }
}
