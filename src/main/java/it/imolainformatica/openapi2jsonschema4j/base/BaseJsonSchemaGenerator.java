package it.imolainformatica.openapi2jsonschema4j.base;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.parser.SwaggerParser;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BaseJsonSchemaGenerator {

	@Getter
	protected Map<String, JsonNode> generatedObjects = new HashMap<String, JsonNode>();
	protected static final String ORIGINAL_REF = "originalRef";
	protected static final String HTTP_JSON_SCHEMA_ORG_DRAFT_04_SCHEMA = "http://json-schema.org/draft-04/schema#";
	protected static final String $SCHEMA = "$schema";
	protected static final String DEFINITIONS2 = "#/definitions/";
	protected static final String TITLE2 = "title";
	protected static final String ADDITIONAL_PROPERTIES = "additionalProperties";
	protected static final String DEFINITIONS = "definitions";
	protected static final String MAX_ITEMS = "maxItems";
	protected static final String MIN_ITEMS = "minItems";
	protected static final String REQUIRED = "required";
	protected static final String PROPERTIES = "properties";
	protected static final String ITEMS = "items";
	protected static final String TYPE = "type";
	@Getter
	protected Set<String> messageObjects = new HashSet<String>();
	@Getter
	private Map<String, Model> objectsDefinitions = new HashMap<String, Model>();

	
	protected Swagger readFromInterface20(File interfaceFile) {
		
		Swagger swagger = new SwaggerParser().read(interfaceFile.getAbsolutePath());
		objectsDefinitions = swagger.getDefinitions();
		for (Map.Entry<String, Path> entry : swagger.getPaths().entrySet()) {
			String k = entry.getKey();
			Path v = entry.getValue();
			log.info(k + "=" + v);
			for (Operation op : v.getOperations()) {
				log.info("Operation={}", op.getOperationId());
				Optional<Parameter> body = op.getParameters().stream().filter(c -> "body".equalsIgnoreCase(c.getIn()))
						.findFirst();
				if (body.isPresent()) {
					BodyParameter bp = (BodyParameter) body.get();
					log.info("Request schema={}", bp.getSchema().getReference());
					messageObjects.add(bp.getSchema().getReference());

				}
				for (Map.Entry<String, Response> entry2 : op.getResponses().entrySet()) {
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
				}
			}

		}
		return swagger;
	}
}
