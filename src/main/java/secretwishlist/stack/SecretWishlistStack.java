package secretwishlist.stack;

import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;
import software.amazon.awscdk.services.apigateway.LambdaIntegration;
import software.amazon.awscdk.services.apigateway.Method;
import software.amazon.awscdk.services.apigateway.Resource;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.BillingMode;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;

import java.util.HashMap;

public class SecretWishlistStack extends Stack {

    public SecretWishlistStack(final Construct parent, final String id) {
        this(parent, id, null);
    }

    public SecretWishlistStack(final Construct parent, final String id, final StackProps props) {
        super(parent, id, props);

        Table wishlistsTable = Table.Builder.create(this, "wishlistsTable")
                .tableName("WishlistsTable")
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .partitionKey(
                        Attribute.builder()
                                .name("id")
                                .type(AttributeType.STRING)
                                .build())
                .build();

        HashMap<String, String> createWishlistLambdaEnvVarables = new HashMap<>();
        createWishlistLambdaEnvVarables.put("wishlistsTable", wishlistsTable.getTableName());

        final Function createWishlistLambda = Function.Builder.create(this, "CreateWishlistHandler")
                .runtime(Runtime.JAVA_8)
                .code(Code.fromAsset("target/secret-wishlist-0.1-jar-with-dependencies.jar"))
                .handler("secretwishlist.lambda.CreateWishlist::handleRequest")
                .environment(createWishlistLambdaEnvVarables)
                .build();

        RestApi api = RestApi.Builder.create(this, "SecretWishlistApi")
                .restApiName("secret-wishlist-api")
                .build();

        Resource createWishlistResource = Resource.Builder.create(this, "createWishlistResource")
                .pathPart("create-wishlist")
                .parent(api.getRoot())
                .build();

        LambdaIntegration createWishlistIntegration = LambdaIntegration.Builder.create(createWishlistLambda)
                .proxy(true)
                .build();

        Method createWishlistPost = Method.Builder.create(this, "createWishlistPost")
                .httpMethod("POST")
                .resource(createWishlistResource)
                .integration(createWishlistIntegration)
                .build();

    }
}
