package org.sep.paypalservice.model;

public class ProductsCreateRequest extends CreateRequest<Product> {

    private static final String PRODUCTS_PATH = "/v1/catalogs/products";

    public ProductsCreateRequest() {
        super(PRODUCTS_PATH, Product.class);
    }
}