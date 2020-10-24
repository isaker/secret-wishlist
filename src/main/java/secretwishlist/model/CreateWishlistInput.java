package secretwishlist.model;

public class CreateWishlistInput {

    private String name;
    private String secret;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {

        return name;
    }
}
