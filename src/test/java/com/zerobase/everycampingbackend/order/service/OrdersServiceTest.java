package com.zerobase.everycampingbackend.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zerobase.everycampingbackend.domain.cart.entity.CartProduct;
import com.zerobase.everycampingbackend.domain.cart.repository.CartRepository;
import com.zerobase.everycampingbackend.domain.order.dto.OrderByCustomerDto;
import com.zerobase.everycampingbackend.domain.order.entity.OrderProduct;
import com.zerobase.everycampingbackend.domain.order.entity.Orders;
import com.zerobase.everycampingbackend.domain.order.form.GetOrdersByCustomerForm;
import com.zerobase.everycampingbackend.domain.order.form.OrderForm;
import com.zerobase.everycampingbackend.domain.order.form.OrderForm.OrderProductForm;
import com.zerobase.everycampingbackend.domain.order.repository.OrderProductRepository;
import com.zerobase.everycampingbackend.domain.order.repository.OrdersRepository;
import com.zerobase.everycampingbackend.domain.order.service.OrderService;
import com.zerobase.everycampingbackend.domain.order.type.OrderStatus;
import com.zerobase.everycampingbackend.domain.product.entity.Product;
import com.zerobase.everycampingbackend.domain.product.repository.ProductRepository;
import com.zerobase.everycampingbackend.domain.product.type.ProductCategory;
import com.zerobase.everycampingbackend.domain.user.entity.Customer;
import com.zerobase.everycampingbackend.domain.user.entity.Seller;
import com.zerobase.everycampingbackend.domain.user.repository.CustomerRepository;
import com.zerobase.everycampingbackend.domain.user.repository.SellerRepository;
import com.zerobase.everycampingbackend.exception.CustomException;
import com.zerobase.everycampingbackend.exception.ErrorCode;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class OrdersServiceTest {

    @Autowired
    OrderService orderService;

    @Autowired
    OrdersRepository ordersRepository;

    @Autowired
    OrderProductRepository orderProductRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    SellerRepository sellerRepository;

    @Autowired
    CartRepository cartRepository;

    @AfterEach
    void clean() {
        cartRepository.deleteAllInBatch();
        orderProductRepository.deleteAllInBatch();
        ordersRepository.deleteAllInBatch();
        customerRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("?????? ??????")
    void orderSuccess() throws Exception {

        //given
        Customer customer = createCustomer("ksj2083@naver.com");

        Product product1 = createProduct("??????1", 300, 5, ProductCategory.TENT);
        Product product2 = createProduct("??????2", 200, 5, ProductCategory.TENT);
        Product product3 = createProduct("??????3", 100, 5, ProductCategory.TENT);

        addToCart(product1, customer, 5);
        addToCart(product2, customer, 4);
        addToCart(product3, customer, 5);

        OrderProductForm form1 = OrderProductForm.builder()
            .productId(product1.getId())
            .quantity(5)
            .build();

        OrderProductForm form2 = OrderProductForm.builder()
            .productId(product2.getId())
            .quantity(4)
            .build();

        OrderForm orderForm = OrderForm.builder()
            .name("?????????")
            .address("????????? ??????")
            .phone("01086352083")
            .request("?????? ?????? ????????????.")
            .orderProductFormList(List.of(form1, form2))
            .build();

        //when
        orderService.order(customer, orderForm);

        //then
        Orders orders = ordersRepository.findAll().get(0);
        assertEquals(customer.getId(), orders.getCustomer().getId());
        assertEquals("?????????", orders.getName());
        assertEquals("????????? ??????", orders.getAddress());
        assertEquals("01086352083", orders.getPhone());

        assertEquals("??????1", orders.getRepresentProductName());
        assertEquals(1500+800, orders.getTotalAmount());

        OrderProduct orderProduct1 = orderProductRepository.findAll().get(0);
        OrderProduct orderProduct2 = orderProductRepository.findAll().get(1);
        Product realProduct1 = productRepository.findById(product1.getId()).orElseThrow();
        Product realProduct2 = productRepository.findById(product2.getId()).orElseThrow();

        assertEquals(realProduct1.getId(), orderProduct1.getProduct().getId());
        assertEquals(5, orderProduct1.getQuantity());
        assertEquals(300 * 5, orderProduct1.getAmount());
        assertEquals(orders.getId(), orderProduct1.getOrders().getId());
        assertEquals(0,realProduct1.getStock());

        assertEquals(realProduct2.getId(), orderProduct2.getProduct().getId());
        assertEquals(4, orderProduct2.getQuantity());
        assertEquals(200 * 4, orderProduct2.getAmount());
        assertEquals(orders.getId(), orderProduct2.getOrders().getId());
        assertEquals(1,realProduct2.getStock());

        List<CartProduct> cartProductList = cartRepository.findAll();
        assertEquals(1,cartProductList.size());
        assertEquals(product3.getId(), cartProductList.get(0).getProduct().getId());
    }

    @Test
    @DisplayName("?????? ?????? - ?????? ???????????? ?????? ??????")
    void createOrderAmountUnder1000() throws Exception {

        //given
        Customer customer = createCustomer("ksj2083@naver.com");
        Long productId1 = createProduct("??????1", 300, 5, ProductCategory.TENT).getId();
        Long productId2 = createProduct("??????2", 200, 5, ProductCategory.TENT).getId();

        OrderProductForm form1 = OrderProductForm.builder().productId(productId1)
            .quantity(5)
            .build();

        OrderProductForm form2 = OrderProductForm.builder().productId(productId2)
            .quantity(6)
            .build();

        OrderForm orderForm = OrderForm.builder()
            .name("?????????")
            .address("????????? ??????")
            .phone("01086352083")
            .request("?????? ?????? ????????????.")
            .orderProductFormList(List.of(form1, form2))
            .build();

        //when
        CustomException ex = (CustomException) assertThrows(RuntimeException.class, () -> {
            orderService.order(customer, orderForm);
        });

        //then
        assertEquals(ErrorCode.PRODUCT_NOT_ENOUGH_STOCK, ex.getErrorCode());

        Product product1 = productRepository.findById(productId1).orElseThrow();
        assertEquals(5, product1.getStock());

        assertTrue(orderProductRepository.findAll().isEmpty());
    }

    @Test
    @DisplayName("?????? ???????????? ?????? ??????")
    void getOrdersByCustomerSuccess() throws Exception {

        //given
        Customer customer = createCustomer("ksj2083@naver.com");
        Long productId1 = createProduct("??????1", 300, 10, ProductCategory.TENT).getId();
        Long productId2 = createProduct("??????2", 200, 10, ProductCategory.TENT).getId();

        OrderProductForm form1 = OrderProductForm.builder().productId(productId1)
            .quantity(5)
            .build();

        OrderProductForm form2 = OrderProductForm.builder().productId(productId2)
            .quantity(4)
            .build();

        OrderForm orderForm = OrderForm.builder()
            .name("?????????")
            .address("????????? ??????")
            .phone("01086352083")
            .request("?????? ?????? ????????????.")
            .orderProductFormList(List.of(form1, form2))
            .build();

        orderService.order(customer, orderForm);

        PageRequest pageRequest = PageRequest.of(0, 5);
        GetOrdersByCustomerForm form = GetOrdersByCustomerForm.builder().build();

        //when
        Page<OrderByCustomerDto> result = orderService.getOrdersByCustomer(form,
            customer.getId(), pageRequest);

        //then
        OrderByCustomerDto dto = result.getContent().get(0);

        assertEquals(2, dto.getOrderProductCount());
        assertEquals(1500+800, dto.getTotalAmount());
    }

    @Test
    @DisplayName("???????????? ??????")
    void confirmSuccess() throws Exception {

        //given
        Customer customer = createCustomer("ksj2083@naver.com");

        Long productId1 = createProduct("??????1", 300, 10, ProductCategory.TENT).getId();

        OrderProductForm form1 = OrderProductForm.builder().productId(productId1)
            .quantity(5)
            .build();

        OrderForm orderForm = OrderForm.builder()
            .orderProductFormList(List.of(form1))
            .build();

        orderService.order(customer, orderForm);

        OrderProduct orderProduct = orderProductRepository.findAll().get(0);
        Long orderProductId = orderProduct.getId();

        //when
        orderService.confirm(customer, orderProductId);

        //then
        OrderProduct result = orderProductRepository.findAll().get(0);
        assertEquals(result.getId(), orderProductId);
        assertEquals(result.getStatus(), OrderStatus.CONFIRM);
        assertEquals(LocalDate.now(), result.getConfirmedAt().toLocalDate());
    }


    private Product createProduct(String name, int price, int stock, ProductCategory category) {
        Product product = Product.builder()
            .name(name)
            .category(category)
            .price(price)
            .stock(stock)
            .onSale(true)
            .seller(createSeller())
            .build();

        Product saved = productRepository.save(product);
        return saved;
    }

    private Customer createCustomer(String email) {
        Customer customer = Customer.builder()
            .email(email)
            .nickName("???????????????")
            .build();

        return customerRepository.save(customer);
    }

    private Seller createSeller() {
        Seller seller = Seller.builder()
            .email("seller@naver.com")
            .nickName("??????????????????")
            .build();

        return sellerRepository.save(seller);
    }

    private CartProduct addToCart(Product product, Customer customer, Integer quantity) {
        CartProduct cartProduct = CartProduct.builder()
            .product(product)
            .customer(customer)
            .quantity(quantity)
            .build();

        return cartRepository.save(cartProduct);
    }
}