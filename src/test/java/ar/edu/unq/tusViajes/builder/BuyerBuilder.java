package ar.edu.unq.tusViajes.builder;

import ar.edu.unq.tusViajes.model.Buyer;
import org.springframework.test.util.ReflectionTestUtils;

public class BuyerBuilder {

    private Long id = null;
    private String firstName = "FirstName";
    private String lastName = "LastName";
    private String email = "buyer@example.com";
    private String passwordHash = "$2a$10$hashedPasswordPlaceholder";
    private String phoneNumber = "11223344";
    private String nationalId = "12345678";

    public static BuyerBuilder aBuyer() {
        return new BuyerBuilder();
    }

    public BuyerBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public BuyerBuilder withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public BuyerBuilder withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public BuyerBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public BuyerBuilder withPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
        return this;
    }

    public BuyerBuilder withPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public BuyerBuilder withNationalId(String nationalId) {
        this.nationalId = nationalId;
        return this;
    }

    public Buyer build() {
        Buyer buyer = new Buyer(firstName, lastName, email, passwordHash, phoneNumber, nationalId);
        if (id != null) {
            ReflectionTestUtils.setField(buyer, "id", id);
        }
        return buyer;
    }
}
