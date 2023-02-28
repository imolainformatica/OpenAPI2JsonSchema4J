package it.imolainformatica.openapi2jsonschema4j.test;

import static org.junit.Assert.fail;

import org.junit.Test;

import it.imolainformatica.openapi2jsonschema4j.base.AbstractIT;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestGeneration extends AbstractIT{

	@Test
	public void testPetStoreWithStrict() {
		testForSwagger("petstore.json"); 
	}


	@Test
	public void testPetStoreWithStrictAndDateFormatAndPattern() {
		testForSwagger("petstoreDateFormat.json");
	}
	
	@Test
	public void testSwaggerWithoutBody() {
		testForSwagger("petstoreNoBody.json");
	}


}
