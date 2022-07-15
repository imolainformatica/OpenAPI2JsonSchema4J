package it.imolainformatica.openapi2jsonschema4j.base;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.models.Swagger;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BaseJsonSchemaGenerator {

	public static final String APPLICATION_JSON = "application/json";
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
				findRequestBodySchema(op, messageObjects);
				for (String key : op.getResponses().keySet()){
					ApiResponse r = op.getResponses().get(key);
					if (r.getContent()!=null) {
						if (r.getContent().get(APPLICATION_JSON) != null) {
							if (r.getContent().get(APPLICATION_JSON).getSchema().get$ref() != null) {
								log.info("code={} responseSchema={}", key, r.getContent().get(APPLICATION_JSON).getSchema().get$ref());
								messageObjects.add(r.getContent().get(APPLICATION_JSON).getSchema().get$ref());
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

	private void findRequestBodySchema(Operation op, Set<String> messageObjects) {
		if (op.getRequestBody()!=null) {
			if (op.getRequestBody().getContent().get(APPLICATION_JSON)!=null) {
				Schema sc = op.getRequestBody().getContent().get(APPLICATION_JSON).getSchema();
				if (sc != null) {
					log.info("Request schema={}", sc.get$ref());
					if (sc.get$ref()!=null) {
						messageObjects.add(sc.get$ref());
					} else {
						log.warn("Request schema is not a referenced definition!");
					}

				}
			} else {
				log.info("No application body of type 'application/json' found for operation {}",op.getOperationId());
			}
		}
	}
}
