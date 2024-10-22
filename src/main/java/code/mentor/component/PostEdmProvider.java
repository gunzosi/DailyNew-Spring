package code.mentor.component;

import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.edm.provider.*;
import org.apache.olingo.odata2.api.edm.provider.EdmProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PostEdmProvider extends EdmProvider {

    @Override
    public List<Schema> getSchemas() {
        Schema schema = new Schema();
        schema.setNamespace("OData.PostService");

        // Định nghĩa EntityType cho Post
        EntityType postEntityType = new EntityType();
        postEntityType.setName("Post");

        // Định nghĩa key cho Post
        PropertyRef key = new PropertyRef();
        key.setName("Id");
        postEntityType.setKey(Collections.singletonList(key));

        // Định nghĩa các property cho Post
        List<Property> properties = new ArrayList<>();
        properties.add(new SimpleProperty().setName("Id").setType(EdmSimpleTypeKind.Int32));
        properties.add(new SimpleProperty().setName("Title").setType(EdmSimpleTypeKind.String));
        properties.add(new SimpleProperty().setName("Content").setType(EdmSimpleTypeKind.String));
        postEntityType.setProperties(properties);

        // Định nghĩa EntitySet cho Post
        EntitySet postEntitySet = new EntitySet();
        postEntitySet.setName("Posts");
        postEntitySet.setEntityType(postEntityType.getFullyQualifiedName());

        // Định nghĩa EntityContainer
        EntityContainer entityContainer = new EntityContainer();
        entityContainer.setName("PostContainer");
        entityContainer.setEntitySets(Collections.singletonList(postEntitySet));

        schema.setEntityTypes(Collections.singletonList(postEntityType));
        schema.setEntityContainers(Collections.singletonList(entityContainer));

        return Collections.singletonList(schema);
    }
}
