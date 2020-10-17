package secretwishlist.stack;

import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Duration;
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
import software.amazon.awscdk.services.iam.IManagedPolicy;
import software.amazon.awscdk.services.iam.ManagedPolicy;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
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

        // TABLES

        Table wishlistsTable = Table.Builder.create(this, "wishlistsTable")
                .tableName("WishlistsTable")
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .partitionKey(
                        Attribute.builder()
                                .name("id")
                                .type(AttributeType.STRING)
                                .build())
                .build();

        // ENVIRONMENT VARIABLES

        HashMap<String, String> wishlistLambdasEnvVariables = new HashMap<>();
        wishlistLambdasEnvVariables.put("wishlistsTable", wishlistsTable.getTableName());

        // ROLES

        ArrayList<IManagedPolicy> createWishlistRolePolicies = new ArrayList<>();
        createWishlistRolePolicies.add(ManagedPolicy.fromAwsManagedPolicyName("service-role/AWSLambdaBasicExecutionRole"));
        createWishlistRolePolicies.add(ManagedPolicy.fromAwsManagedPolicyName("AmazonDynamoDBFullAccess"));

        Role wishlistRole = Role.Builder.create(this, "WishlistRole")
                .managedPolicies(createWishlistRolePolicies)
                .description("Used by Lambdas handling Wishlists")
                .assumedBy(new ServicePrincipal("lambda.amazonaws.com"))
                .build();

        // LAMBDAS

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

        final Function removeItemLambda = Function.Builder.create(this, "RemoveItemHandler")
                .runtime(Runtime.JAVA_8)
                .code(Code.fromAsset("target/secret-wishlist-0.1-jar-with-dependencies.jar"))
                .handler("secretwishlist.lambda.RemoveItem::handleRequest")
                .environment(wishlistLambdasEnvVariables)
                .memorySize(1024)
                .timeout(Duration.seconds(15))
                .role(wishlistRole)
                .build();

        // API GATEWAYS

        RestApi api = RestApi.Builder.create(this, "SecretWishlistApi")
                .restApiName("secret-wishlist-api")
                .build();

        // CREATE WISHLIST ENDPOINT

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

        // GET WISHLIST ENDPOINT

        Resource wishlistBaseResource = Resource.Builder.create(this, "wishlistBaseResource")
                .pathPart("wishlist")
                .parent(api.getRoot())
                .build();
        Resource wishlistIdResource = Resource.Builder.create(this, "wishlistIdResource")
                .pathPart("{id}")
                .parent(wishlistBaseResource)
                .build();

        LambdaIntegration getWishlistIntegration = LambdaIntegration.Builder.create(getWishlistLambda)
                .proxy(true)
                .build();

        Method getWishlistMethod = Method.Builder.create(this, "getWishlistMethod")
                .httpMethod("GET")
                .resource(wishlistIdResource)
                .integration(getWishlistIntegration)
                .build();

        // ADD ITEM ENDPOINT

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

        // REMOVE ITEM ENDPOINT

        Resource itemIdResource = Resource.Builder.create(this, "itemIdResource")
                .pathPart("{itemId}")
                .parent(itemResource)
                .build();

        LambdaIntegration removeItemIntegration = LambdaIntegration.Builder.create(removeItemLambda)
                .proxy(true)
                .build();

        Method deleteItemMethod = Method.Builder.create(this, "deleteItemMethod")
                .httpMethod("DELETE")
                .resource(itemIdResource)
                .integration(removeItemIntegration)
                .build();

        // UPDATE ITEM ENDPOINT

        LambdaIntegration updateItemIntegration = LambdaIntegration.Builder.create(removeItemLambda)
                .proxy(true)
                .build();

        Method updateItemMethod = Method.Builder.create(this, "updateItemMethod")
                .httpMethod("POST")
                .resource(itemIdResource)
                .integration(updateItemIntegration)
                .build();

        // GET ITEM ENDPOINT

        LambdaIntegration getItemIntegration = LambdaIntegration.Builder.create(removeItemLambda)
                .proxy(true)
                .build();

        Method getItemMethod = Method.Builder.create(this, "getItemMethod")
                .httpMethod("GET")
                .resource(itemIdResource)
                .integration(getItemIntegration)
                .build();

    }
}
