package it.imolainformatica.openapi2jsonschema4j.base;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;

public interface IJsonSchemaOutputWriter {

	void saveJsonSchemaFiles(Map<String, JsonNode> schemas, File outputDirectory)
			throws JsonGenerationException, JsonMappingException, IOException;
}
