package secretwishlist.stack;

import software.amazon.awscdk.core.App;

public final class SecretWishlistApp {
    public static void main(final String args[]) {
        App app = new App();

        new SecretWishlistStack(app, "SecretWishlistStack");

        app.synth();
    }
}
