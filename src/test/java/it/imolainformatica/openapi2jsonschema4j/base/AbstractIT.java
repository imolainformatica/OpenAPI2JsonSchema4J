package it.imolainformatica.openapi2jsonschema4j.base;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.processors.syntax.SyntaxValidator;
import io.swagger.oas.inflector.examples.ExampleBuilder;
import io.swagger.oas.inflector.examples.models.Example;
import io.swagger.oas.inflector.processors.JsonNodeExampleSerializer;
import io.swagger.parser.OpenAPIParser;
import io.swagger.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import it.imolainformatica.openapi2jsonschema4j.builder.JsonSchemaGeneratorBuilder;
import it.imolainformatica.openapi2jsonschema4j.impl.JsonSchemaOutputWriter;
import junit.framework.Assert;
import lombok.extern.slf4j.Slf4j;

import static org.junit.Assert.fail;

@Slf4j
public class AbstractIT {


	protected File loadFromResourceFile(String resourceName) {
		log.debug("loading resource {}",resourceName);
		File file = new File(
				getClass().getClassLoader().getResource(resourceName).getFile()
			);
		return file;
		
	}

    protected void testForSwagger(String swaggerFile) {
        log.info("Test for swagger {}", swaggerFile);
        File f = loadFromResourceFile(swaggerFile);
        IJsonSchemaGenerator jsg = new JsonSchemaGeneratorBuilder().withOutputSchemaVersion(JsonSchemaVersion.DRAFT_V4).withStrictGeneration(true).build();
        try {
            Map<String, JsonNode> gen = jsg.generate(f);
            new JsonSchemaOutputWriter().saveJsonSchemaFiles(gen, new File("target/generatedJsonSchema/"+swaggerFile));
            testGeneratedJsonSchema(f, gen);
        } catch (Exception e) {
            log.error("Unexpected exception" + e.getMessage(), e);
            fail("Unexpected exception");

        }
    }

    private void testGeneratedJsonSchema(File f, Map<String, JsonNode> gen) throws ProcessingException, IOException {
        ParseOptions parseOptions = new ParseOptions();
        parseOptions.setResolve(true); // implicit
        SwaggerParseResult result = new OpenAPIParser().readLocation(f.getAbsolutePath(),null,parseOptions);
        OpenAPI swagger = result.getOpenAPI();

        Map<String, Schema> definitions = new HashMap<String,Schema>();
        if (swagger.getComponents() != null && swagger.getComponents().getSchemas() != null) {
          definitions = swagger.getComponents().getSchemas();
        }

        for (Map.Entry<String, JsonNode> entry : gen.entrySet()) {
            String objName = entry.getKey();
            JsonNode jsonSchemaNode = entry.getValue();
            Schema model = definitions.get(objName);
            log.info("Generating object example for model {}",objName);
            Example example = ExampleBuilder.fromSchema(model, definitions);
            SimpleModule simpleModule = new SimpleModule().addSerializer(new JsonNodeExampleSerializer());
            Json.mapper().registerModule(simpleModule);
            String jsonExample = Json.pretty(example);
            log.info("jsonExample={}",jsonExample);
            JsonSchemaFactory schemaFactory = JsonSchemaFactory.byDefault();
            JsonSchema jsonSchema = schemaFactory.getJsonSchema(jsonSchemaNode);
            validateJsonSchemaSyntax(schemaFactory.getSyntaxValidator(), jsonSchemaNode);
            if (jsonExample.equals(null)) {
                log.info("JsonExample is valid, proceeding with validation");
                //Validation on example is possible only if components are defined inside the swagger file
                ProcessingReport rep = jsonSchema.validate(new ObjectMapper().readTree(jsonExample));
                log.info("processing report for model {} = {}",objName,rep);
                Assert.assertTrue("Il json generato non è valido in base al json schema per l'oggetto "+objName, rep.isSuccess());
            }            
        }
    }

    private void validateJsonSchemaSyntax(SyntaxValidator syntaxValidator, JsonNode node) {
        ProcessingReport rep =  syntaxValidator.validateSchema(node);
        Assert.assertTrue(rep.isSuccess());
    }
}
