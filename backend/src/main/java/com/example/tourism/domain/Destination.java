package com.example.tourism.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "destinations")
public class Destination {

    // 主键 ID，数据库自动生成。
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 目的地名称，例如 Zhangjiajie Sky Forest。
    @Column(nullable = false, length = 80)
    private String name;

    // 目的地所在省份。
    @Column(nullable = false, length = 40)
    private String province;

    // 旅游主题，例如 Mountain、Island、Culture。
    @Column(nullable = false, length = 40)
    private String theme;

    // 目的地介绍文案。
    @Column(nullable = false, length = 500)
    private String description;

    // 前端卡片使用的封面图片 URL。
    @Column(nullable = false, length = 500)
    private String coverImage;

    // 评分，前端会展示一位小数。
    @Column(nullable = false)
    private Double rating;

    // 热度分，用来表达目的地受欢迎程度。
    @Column(nullable = false)
    private Integer popularityScore;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Integer getPopularityScore() {
        return popularityScore;
    }

    public void setPopularityScore(Integer popularityScore) {
        this.popularityScore = popularityScore;
    }
}
