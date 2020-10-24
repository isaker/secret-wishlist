package secretwishlist.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class Secret {

    private String id;
    private String secret;

    public Secret() {
    }

    public Secret(String id, String secret) {

        this.id = id;
        this.secret = secret;
    }

    @DynamoDbPartitionKey

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
