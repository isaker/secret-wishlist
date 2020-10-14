package secretwishlist.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.ArrayList;
import java.util.List;

public class ItemsAttributeConverter implements AttributeConverter<List<Item>> {

    private Gson gson = new Gson();

    @Override
    public AttributeValue transformFrom(List<Item> items) {
        return null;
    }

    @Override
    public ArrayList<Item> transformTo(AttributeValue attributeValue) {
        return gson.fromJson(attributeValue.s(), new TypeToken<List<Item>>(){}.getType());
    }

    @Override
    public EnhancedType type() {
        return null;
    }

    @Override
    public AttributeValueType attributeValueType() {
        return AttributeValueType.S;
    }
}
