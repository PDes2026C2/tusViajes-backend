### Integration Tests (Good) vs. Implementation Details (Bad)

```java
// GOOD: Tests observable behavior
@Test
void userCanCheckoutWithValidCart() {
    Cart cart = new Cart();
    cart.add(product);
    
    CheckoutResult result = checkoutService.checkout(cart, paymentMethod);
    
    assertEquals("confirmed", result.getStatus());
}


// GOOD: Using Builders 
@Test
void userCanCheckoutWithValidCartUsingBuilder() {
    Cart cart = aCart().withProduct(product).build();

    CheckoutResult result = checkoutService.checkout(cart, paymentMethod);
    
    assertEquals("confirmed", result.getStatus());
}

// BAD: Tests implementation details
@Test
void checkoutCallsPaymentServiceProcess() {
    PaymentService mockPayment = Mockito.mock(PaymentService.class);
    CheckoutService checkoutService = new CheckoutService(mockPayment);
    
    checkoutService.checkout(cart, payment);
    
    // Verifies internal method call, coupling the test to the implementation
    Mockito.verify(mockPayment).process(cart.getTotal());
}
```

### Verifying through Interface vs. Bypassing Interface

```java
// BAD: Bypasses interface to verify
@Test
void createUserSavesToDatabase() throws SQLException {
    userService.createUser(new UserDto("Alice"));
    
    // Breaks encapsulation by querying the database directly
    ResultSet row = db.query("SELECT * FROM users WHERE name = ?", "Alice");
    assertTrue(row.next()); 
}

// GOOD: Verifies through interface
@Test
void createUserMakesUserRetrievable() {
    User user = userService.createUser(new UserDto("Alice"));
    
    User retrieved = userService.getUser(user.getId());
    
    assertEquals("Alice", retrieved.getName());
}
```

### Tautological Tests

```java
// BAD: Expected value is recomputed the way the code computes it
@Test
void calculateTotalSumsLineItemsTautological() {
    List<Item> items = List.of(new Item(10), new Item(5));
    
    int expected = items.stream().mapToInt(Item::getPrice).sum();
    
    assertEquals(expected, calculator.calculateTotal(items));
}

// GOOD: Expected value is an independent, known literal
@Test
void calculateTotalSumsLineItems() {
    List<Item> items = List.of(new Item(10), new Item(5));
    
    assertEquals(15, calculator.calculateTotal(items));
}
```

---
