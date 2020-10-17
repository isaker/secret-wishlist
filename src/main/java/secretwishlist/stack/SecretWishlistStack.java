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
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .partitionKey(
                        Attribute.builder()
                                .name("id")
                                .type(AttributeType.STRING)
                                .build())
                .build();

        Table secretsTable = Table.Builder.create(this, "secretsTable")
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

        HashMap<String, String> basicAuthLambdaEnvVariables = new HashMap<>();
        basicAuthLambdaEnvVariables.put("secretsTable", secretsTable.getTableName());

        // ROLES

        ArrayList<IManagedPolicy> wishlistRolePolicies = new ArrayList<>();
        wishlistRolePolicies.add(ManagedPolicy.fromAwsManagedPolicyName("service-role/AWSLambdaBasicExecutionRole"));
        wishlistRolePolicies.add(ManagedPolicy.fromAwsManagedPolicyName("AmazonDynamoDBFullAccess"));

        Role wishlistRole = Role.Builder.create(this, "WishlistRole")
                .managedPolicies(wishlistRolePolicies)
                .description("Used by Lambdas handling Wishlists.")
                .assumedBy(new ServicePrincipal("lambda.amazonaws.com"))
                .build();

        ArrayList<IManagedPolicy> basicAuthRolePolicies = new ArrayList<>();
        basicAuthRolePolicies.add(ManagedPolicy.fromAwsManagedPolicyName("service-role/AWSLambdaBasicExecutionRole"));
        basicAuthRolePolicies.add(ManagedPolicy.fromAwsManagedPolicyName("AmazonDynamoDBReadOnlyAccess"));

        Role basicAuthRole = Role.Builder.create(this, "BasicAuthRole")
                .managedPolicies(basicAuthRolePolicies)
                .description("Used by Basic Authorizer Lambda to read secrets from DynamoDB.")
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
                .handler("secretwishlist.lambda.ItemHandler::handleRequest")
                .environment(wishlistLambdasEnvVariables)
                .memorySize(1024)
                .timeout(Duration.seconds(15))
                .role(wishlistRole)
                .build();

        final Function basicAuthorizerLambda = Function.Builder.create(this, "BasicAuthorizerLambda")
                .runtime(Runtime.PYTHON_3_7)
                .code(Code.fromAsset("python/authorizer"))
                .handler("BasicAuthorizer.auth_handler")
                .environment(basicAuthLambdaEnvVariables)
                .role(basicAuthRole)
                .build();

        // API GATEWAYS

        RestApi wishlistApi = RestApi.Builder.create(this, "SecretWishlistApi")
                .restApiName("secret-wishlist-api")
                .build();

        RequestValidator bodyValidator = RequestValidator.Builder.create(this, "bodyValidator")
                .validateRequestBody(true)
                .restApi(wishlistApi)
                .build();

        // CREATE WISHLIST ENDPOINT

        Resource createWishlistResource = Resource.Builder.create(this, "createWishlistResource")
                .pathPart("create-wishlist")
                .parent(wishlistApi.getRoot())
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
                .parent(wishlistApi.getRoot())
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

        HashMap<String, JsonSchema> newItemSchemaproperties = new HashMap<>();
        newItemSchemaproperties.put("url", JsonSchema.builder().type(JsonSchemaType.STRING).build());
        newItemSchemaproperties.put("description", JsonSchema.builder().type(JsonSchemaType.STRING).minLength(1).maxLength(140).build());
        ArrayList<String> newItemSchemaRequiredProperties = new ArrayList<>();
        newItemSchemaRequiredProperties.add("description");

        JsonSchema newItemSchema = JsonSchema.builder()
                .properties(newItemSchemaproperties)
                .required(newItemSchemaRequiredProperties)
                .build();

        Model newItemModel = Model.Builder.create(this, "newItemModel")
                .contentType("application/json")
                .restApi(wishlistApi)
                .description("Model used when adding a new Item to a Wishlist.")
                .schema(newItemSchema)
                .build();

        Resource itemResource = Resource.Builder.create(this, "itemResource")
                .pathPart("item")
                .parent(wishlistIdResource)
                .build();

        LambdaIntegration addItemIntegration = LambdaIntegration.Builder.create(addItemLambda)
                .proxy(true)
                .build();

        ArrayList<String> identitySources = new ArrayList<>();
        identitySources.add(IdentitySource.header("Authorization"));
        RequestAuthorizer basicAuthorizer = RequestAuthorizer.Builder.create(this, "basicAuthorizer")
                .handler(basicAuthorizerLambda)
                .identitySources(identitySources)
                .build();

        HashMap<String, Model> postItemMethodModels = new HashMap<>();
        postItemMethodModels.put("application/json", newItemModel);

        Method postItemMethod = Method.Builder.create(this, "postItemMethod")
                .httpMethod("POST")
                .resource(itemResource)
                .integration(addItemIntegration)
                .options(MethodOptions.builder()
                        .requestModels(postItemMethodModels)
                        .requestValidator(bodyValidator)
                        .authorizationType(AuthorizationType.CUSTOM)
                        .authorizer(basicAuthorizer)
                        .build())
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
                .options(MethodOptions.builder()
                        .authorizationType(AuthorizationType.CUSTOM)
                        .authorizer(basicAuthorizer)
                        .build())
                .build();

        // UPDATE ITEM ENDPOINT

        LambdaIntegration updateItemIntegration = LambdaIntegration.Builder.create(removeItemLambda)
                .proxy(true)
                .build();

        Method updateItemMethod = Method.Builder.create(this, "updateItemMethod")
                .httpMethod("POST")
                .resource(itemIdResource)
                .integration(updateItemIntegration)
                .options(MethodOptions.builder()
                        .authorizationType(AuthorizationType.CUSTOM)
                        .authorizer(basicAuthorizer)
                        .build())
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
