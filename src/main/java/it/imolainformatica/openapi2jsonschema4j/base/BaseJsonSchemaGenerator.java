package it.imolainformatica.openapi2jsonschema4j.base;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.models.Model;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.parser.OpenAPIParser;
import io.swagger.parser.SwaggerParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BaseJsonSchemaGenerator {

	@Getter
	protected Map<String, JsonNode> generatedObjects = new HashMap<String, JsonNode>();
	protected static final String ORIGINAL_REF = "originalRef";
	protected static final String HTTP_JSON_SCHEMA_ORG_DRAFT_04_SCHEMA = "http://json-schema.org/draft-04/schema#";
	protected static final String $SCHEMA = "$schema";
	protected static final String DEFINITIONS2 = "#/components/schemas/";
	protected static final String TITLE2 = "title";
	protected static final String ADDITIONAL_PROPERTIES = "additionalProperties";
	//protected static final String DEFINITIONS = "definitions";
	protected static final String MAX_ITEMS = "maxItems";
	protected static final String MIN_ITEMS = "minItems";
	protected static final String REQUIRED = "required";
	protected static final String PROPERTIES = "properties";
	protected static final String ITEMS = "items";
	protected static final String TYPE = "type";
	@Getter
	protected Set<String> messageObjects = new HashSet<String>();
	@Getter
	private Map<String, Schema> objectsDefinitions = new HashMap<String, Schema>();

	
	protected Swagger readFromInterface20(File interfaceFile) {

		SwaggerParseResult result = new OpenAPIParser().readLocation(interfaceFile.getAbsolutePath(),null,null);
		OpenAPI swagger = result.getOpenAPI();
		//Swagger swagger = new SwaggerParser().read(interfaceFile.getAbsolutePath());
		objectsDefinitions = swagger.getComponents().getSchemas();
		for (Map.Entry<String, PathItem> entry : swagger.getPaths().entrySet()) {
			String k = entry.getKey();
			PathItem v = entry.getValue();
			log.info(k + "=" + v);
			for (Operation op : v.readOperations()) {
				log.info("Operation={}", op.getOperationId());
				getRequestBodySchema(op, messageObjects);
				//Optional<Parameter> body = op.getParameters().stream().filter(c -> "body".equalsIgnoreCase(c.getIn()))
				//		.findFirst();
				//if (body.isPresent()) {
					//BodyParameter bp = (BodyParameter) body.get()


				//}
				for (String key : op.getResponses().keySet()){
					ApiResponse r = op.getResponses().get(key);
					if (r.getContent()!=null) {
						if (r.getContent().get("application/json") != null) {
							if (r.getContent().get("application/json").getSchema().get$ref() != null) {
								log.info("code={} responseSchema={}", key, r.getContent().get("application/json").getSchema().get$ref());
								messageObjects.add(r.getContent().get("application/json").getSchema().get$ref());
							} else {
								log.warn("code={} response schema is not a referenced definition! type={}", key, r.getContent().get("application/json").getClass());
							}
						}
					}
				}



				/*for (Map.Entry<String, ApiResponse> entry2 : op.getResponses()) {
					String rk = entry2.getKey();
					Response r = entry2.getValue();
					if (r.getResponseSchema() != null) {
						if (r.getResponseSchema().getReference()!=null) {
							messageObjects.add(r.getResponseSchema().getReference());
							log.info("code={} responseSchema={}", rk, r.getResponseSchema().getReference());
						} else {
							log.warn("code={} response schema is not a referenced definition! type={}",rk,r.getResponseSchema().getClass());
						}

					}
				}*/
			}

		}

		return null;

	}

	private void getRequestBodySchema(Operation op, Set<String> messageObjects) {
		if (op.getRequestBody()!=null) {
			Schema sc = op.getRequestBody().getContent().get("application/json").getSchema();
			if (sc!=null) {
				log.info("Request schema={}", sc.get$ref());
				messageObjects.add(sc.get$ref());
			}
		}
	}
}
