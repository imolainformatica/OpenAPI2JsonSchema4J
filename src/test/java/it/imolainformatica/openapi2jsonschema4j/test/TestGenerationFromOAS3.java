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


}
