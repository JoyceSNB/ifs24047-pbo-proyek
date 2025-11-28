package org.delcom.app.dto;

import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;

public class FlowerForm {
    private Long id;
    private String name;
    private String variety;
    private Integer stock;
    private Double price;
    private MultipartFile image; // Menangkap file upload

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getVariety() { return variety; }
    public void setVariety(String variety) { this.variety = variety; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public MultipartFile getImage() { return image; }
    public void setImage(MultipartFile image) { this.image = image; }
}