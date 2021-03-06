package it.imolainformatica.openapi2jsonschema4j.test;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

import io.swagger.inflector.examples.ExampleBuilder;
import io.swagger.inflector.examples.models.Example;
import io.swagger.inflector.processors.JsonNodeExampleSerializer;
import io.swagger.models.Model;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import io.swagger.util.Json;
import it.imolainformatica.openapi2jsonschema4j.base.AbstractIT;
import it.imolainformatica.openapi2jsonschema4j.base.IJsonSchemaGenerator;
import it.imolainformatica.openapi2jsonschema4j.base.JsonSchemaVersion;
import it.imolainformatica.openapi2jsonschema4j.builder.JsonSchemaGeneratorBuilder;
import it.imolainformatica.openapi2jsonschema4j.impl.JsonSchemaOutputWriter;
import junit.framework.Assert;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestGeneration extends AbstractIT{

	@Test
	public void testPetStoreWithStrict() {
		testForSwagger("petstore.json"); 
	}
	
	/*@Test
	public void testDefaultSwaggerYamlWithStrict() {
		testForSwagger("APIAC_AnagrafeClientiAPI_v1.0.yaml"); 
	}*/

	private void testForSwagger(String swaggerFile) {
		log.info("Test for swagger {}",swaggerFile);
		File f = loadFromResourceFile(swaggerFile);
		IJsonSchemaGenerator jsg = new JsonSchemaGeneratorBuilder().withOutputSchemaVersion(JsonSchemaVersion.DRAFT_V4).withStrictGeneration(true).build();
		try {
			Map<String, JsonNode> gen = jsg.generate(f);
			new JsonSchemaOutputWriter().saveJsonSchemaFiles(gen,new File("target/generatedJsonSchema"));
			testGeneratedJsonSchema(f,gen);
		} catch (Exception e) {
			fail("Unexpected exception");
			log.error("Unexpected exception"+e.getMessage(),e);
		}
	}

	private void testGeneratedJsonSchema(File f, Map<String, JsonNode> gen) throws ProcessingException, IOException {
		Swagger swagger = new SwaggerParser().read(f.getAbsolutePath());
		Map<String, Model> definitions = swagger.getDefinitions();
		
		for (Map.Entry<String, JsonNode> entry : gen.entrySet()) {
			String objName = entry.getKey();
			JsonNode jsonSchemaNode = entry.getValue();
			Model model = definitions.get(objName);
			log.info("Generating object example for model {}",objName);
			Example example = ExampleBuilder.fromModel(objName, model, definitions, new HashSet<String>());
			SimpleModule simpleModule = new SimpleModule().addSerializer(new JsonNodeExampleSerializer());
			Json.mapper().registerModule(simpleModule);
			String jsonExample = Json.pretty(example);
			log.info("jsonExample={}",jsonExample);
			JsonSchemaFactory schemaFactory = JsonSchemaFactory.byDefault();
	        JsonSchema jsonSchema = schemaFactory.getJsonSchema(jsonSchemaNode);
	        ProcessingReport rep = jsonSchema.validate(new ObjectMapper().readTree(jsonExample));
	        log.info("processing report for model {} = {}",objName,rep);
	        Assert.assertTrue("Il json generato non è valido in base al json schema per l'oggetto "+objName, rep.isSuccess());
	        
		}

		

		
	}
}
