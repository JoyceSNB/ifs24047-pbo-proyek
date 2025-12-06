package org.delcom.app.dto;

import org.springframework.web.multipart.MultipartFile;
import java.util.UUID; 

public class FlowerForm {
    
    private UUID id;
    
    private String flowerName;
    private String species;
    private Double price;
    private Integer stock;
    private String description;
    
    private MultipartFile image;

    public FlowerForm() {}
    
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getFlowerName() { return flowerName; }
    public void setFlowerName(String flowerName) { this.flowerName = flowerName; }

    public String getSpecies() { return species; }
    public void setSpecies(String species) { this.species = species; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public MultipartFile getImage() { return image; }
    public void setImage(MultipartFile image) { this.image = image; }
}