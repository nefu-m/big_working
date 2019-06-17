package com.newkeshe.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@EqualsAndHashCode
public class Ivg {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(length = 20)
    private String name;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime beginTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    private Integer location;
    private Integer numbersOfTeacher;
    private Integer count;
    @JsonIgnore
    @OneToMany(mappedBy = "ivg", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<User_Ivg> userIvgs;

    public Ivg(){

    }
    public Ivg(Integer ivgId) {
        this.id = ivgId;
    }
}
