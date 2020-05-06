package com.project.third.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private long id;

    @Column(name="poster", length = 16384)
    private String posterURL;

    @Column(name = "title")
    private String title;

    @Column(name="shortcontent", length = 512)
    private String shortContent;

    @Column(name="content", length = 2048)
    private String content;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Genre> genres;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private Users author;

    @Column(name="postdate")
    private LocalDateTime postDate;

    @Column(name = "isactive")
    private Boolean isActive = true;

    public String listGenres()
    {
        StringBuilder out = new StringBuilder();
        if(!this.genres.isEmpty()) {
            for (Genre genre : this.genres) {
                out.append(genre.getName()).append(",");
            }
        }
        out.deleteCharAt(out.lastIndexOf(","));
        return out.toString();
    }

    public Post(String title, String posterUrl, String shortContent, String content, Set<Genre> genres, Users author) {
        this.title = title;
        this.posterURL = posterUrl;
        this.shortContent = shortContent;
        this.content = content;
        this.author = author;
        this.postDate = LocalDateTime.now();
        this.genres = genres;
        this.isActive = true;
    }

    public Post(String title, String posterUrl, String shortContent, String content, Users author) {
        this.title = title;
        this.posterURL = posterUrl;
        this.shortContent = shortContent;
        this.content = content;
        this.author = author;
        this.postDate = LocalDateTime.now();
        this.genres = new HashSet<Genre>();
        this.isActive = true;
    }

}
