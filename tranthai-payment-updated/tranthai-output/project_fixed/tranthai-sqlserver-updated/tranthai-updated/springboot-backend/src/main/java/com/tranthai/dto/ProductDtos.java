package com.tranthai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

public class ProductDtos {

    @Data
    public static class ProductRequest {
        @NotBlank
        private String name;

        @NotNull
        private Double price;

        private String description;

        @PositiveOrZero
        private Integer stock;

        private String status;   // "Đang bán" | "Hết hàng" | "Ngừng bán"

        private String image;

        private String category;
    }

    @Data
    public static class ProductResponse {
        private Long    id;
        private String  name;
        private Double  price;
        private String  description;
        private Integer stock;
        private String  status;
        private String  image;
        private String  category;
        private Boolean deleted;
        private String  deletedAt;
    }
}
