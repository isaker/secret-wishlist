package secretwishlist.stack;

import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Duration;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;
import software.amazon.awscdk.services.apigateway.*;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.BillingMode;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.iam.*;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;

import java.util.ArrayList;
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

        HashMap<String, String> wishlistLambdasEnvVariables = new HashMap<>();
        wishlistLambdasEnvVariables.put("wishlistsTable", wishlistsTable.getTableName());

        ArrayList<IManagedPolicy> createWishlistRolePolicies = new ArrayList<>();
        createWishlistRolePolicies.add(ManagedPolicy.fromAwsManagedPolicyName("service-role/AWSLambdaBasicExecutionRole"));
        createWishlistRolePolicies.add(ManagedPolicy.fromAwsManagedPolicyName("AmazonDynamoDBFullAccess"));

        Role wishlistRole = Role.Builder.create(this, "WishlistRole")
                .managedPolicies(createWishlistRolePolicies)
                .description("Used by Lambdas handling Wishlists")
                .assumedBy(new ServicePrincipal("lambda.amazonaws.com"))
                .build();

        final Function createWishlistLambda = Function.Builder.create(this, "CreateWishlistHandler")
                .runtime(Runtime.JAVA_8)
                .code(Code.fromAsset("target/secret-wishlist-0.1-jar-with-dependencies.jar"))
                .handler("secretwishlist.lambda.CreateWishlist::handleRequest")
                .environment(wishlistLambdasEnvVariables)
                .memorySize(1024)
                .timeout(Duration.seconds(15))
                .role(wishlistRole)
                .build();

        final Function getWishlistLambda = Function.Builder.create(this, "GetWishlistHandler")
                .runtime(Runtime.JAVA_8)
                .code(Code.fromAsset("target/secret-wishlist-0.1-jar-with-dependencies.jar"))
                .handler("secretwishlist.lambda.GetWishlist::handleRequest")
                .environment(wishlistLambdasEnvVariables)
                .memorySize(1024)
                .timeout(Duration.seconds(15))
                .role(wishlistRole)
                .build();

        final Function addItemLambda = Function.Builder.create(this, "AddItemHandler")
                .runtime(Runtime.JAVA_8)
                .code(Code.fromAsset("target/secret-wishlist-0.1-jar-with-dependencies.jar"))
                .handler("secretwishlist.lambda.AddItem::handleRequest")
                .environment(wishlistLambdasEnvVariables)
                .memorySize(1024)
                .timeout(Duration.seconds(15))
                .role(wishlistRole)
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

        Resource getWishlistBaseResource = Resource.Builder.create(this, "getWishlistBaseResource")
                .pathPart("wishlist")
                .parent(api.getRoot())
                .build();
        Resource wishlistIdResource = Resource.Builder.create(this, "wishlistIdResource")
                .pathPart("{id}")
                .parent(getWishlistBaseResource)
                .build();

        LambdaIntegration getWishlistIntegration = LambdaIntegration.Builder.create(getWishlistLambda)
                .proxy(true)
                .build();

        Method getWishlistMethod = Method.Builder.create(this, "getWishlistMethod")
                .httpMethod("GET")
                .resource(wishlistIdResource)
                .integration(getWishlistIntegration)
                .build();

        Resource itemResource = Resource.Builder.create(this, "itemResource")
                .pathPart("item")
                .parent(wishlistIdResource)
                .build();

        LambdaIntegration addItemIntegration = LambdaIntegration.Builder.create(addItemLambda)
                .proxy(true)
                .build();

        Method postItemMethod = Method.Builder.create(this, "postItemMethod")
                .httpMethod("POST")
                .resource(itemResource)
                .integration(addItemIntegration)
                .build();

    }
}
