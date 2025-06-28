package com.example.ecommerce.model.entity;


import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * CartItem entity representing individual products added to a cart
 * Links a specific product with quantity and pricing information
 */
@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many cart items belong to one cart
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    // Many cart items can reference one product
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    // Store price at time of adding to cart (in case product price changes)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtTimeOfAdd;

    // Constructors
    public CartItem() {}

    public CartItem(Cart cart, Product product, Integer quantity) {
        this.cart = cart;
        this.product = product;
        this.quantity = quantity;
        this.priceAtTimeOfAdd = product.getPrice();
    }

    /**
     * Calculate total price for this cart item (quantity * price)
     */
    public BigDecimal getTotalPrice() {
        return priceAtTimeOfAdd.multiply(BigDecimal.valueOf(quantity));
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Cart getCart() { return cart; }
    public void setCart(Cart cart) { this.cart = cart; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getPriceAtTimeOfAdd() { return priceAtTimeOfAdd; }
    public void setPriceAtTimeOfAdd(BigDecimal priceAtTimeOfAdd) { this.priceAtTimeOfAdd = priceAtTimeOfAdd; }
}