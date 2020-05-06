package com.project.third.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private long id;

    @ManyToOne()
    @JoinColumn(name = "author_id")
    private Users author;

    @ManyToOne()
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(name = "comment", length = 16384)
    private String comment;

    @Column(name = "postdate")
    private LocalDateTime postDate;

    public Comment(Users author, Post post, String comment) {
        this.author = author;
        this.post = post;
        this.comment = comment;
        this.postDate = LocalDateTime.now();
    }

}
