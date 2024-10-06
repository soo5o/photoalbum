package com.squarecross.photoalbum.domain;
import jakarta.persistence.*;
@Entity
@Table(name="photo", schema="photo_album2", uniqueConstraints = {@UniqueConstraint(columnNames = "photo_id")})
public class Photo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "photo_id", unique = true, nullable = false)
    private Long photoId;
    @Column(name = "file_name", unique = false, nullable = false)
    private String fileName;
}
