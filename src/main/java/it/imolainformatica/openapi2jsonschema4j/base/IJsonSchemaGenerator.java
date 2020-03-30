package it.imolainformatica.openapi2jsonschema4j.base;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.models.Swagger;
import lombok.Getter;

public interface IJsonSchemaGenerator {

	public Map<String, JsonNode> generate(File interfaceFile) throws JsonGenerationException, JsonMappingException, IOException, Exception;
}
