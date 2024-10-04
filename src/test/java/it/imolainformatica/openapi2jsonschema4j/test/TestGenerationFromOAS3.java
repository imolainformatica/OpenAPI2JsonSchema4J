package it.imolainformatica.openapi2jsonschema4j.test;

import it.imolainformatica.openapi2jsonschema4j.base.AbstractIT;
import org.junit.Test;

public class TestGenerationFromOAS3 extends AbstractIT {

    @Test
    public void testOAS3() { testForSwagger("petstoreoas3.json");	}

    @Test
    public void testOAS3WithRemoteReferences() { testForSwagger("petstoreoas3-remoteref.json");	}

    @Test
    public void testOAS3WithAdditionalProperties() { testForSwagger("testOASAdditionalProperties.json");	}
    
    @Test
    public void testOAS3WithAdditionalPropertiesFalse() { testForSwagger("testOASAdditionalPropertiesFalse.json");	}

    @Test
    public void testOAS3WithOneOf() { testForSwagger("petstoreoas3Oneof.json");}

    @Test
    public void testOAS3WithObjectTypeNull() { testForSwagger("petstoreoas3ObjectTypeNull.json");}

    @Test
    public void testOAS3WithComponentsInline() { testForSwagger("petstoreoas3ObjectInline.json");	}

    @Test
    public void testOAS3WithAllOf() { testForSwagger("petstoreoas3Allof.json");	}
}
