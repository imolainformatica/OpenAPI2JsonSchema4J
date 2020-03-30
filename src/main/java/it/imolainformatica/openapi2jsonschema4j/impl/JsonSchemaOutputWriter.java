package it.imolainformatica.openapi2jsonschema4j.impl;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.imolainformatica.openapi2jsonschema4j.base.IJsonSchemaOutputWriter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonSchemaOutputWriter implements IJsonSchemaOutputWriter {

	private static final String JSON = ".json";

	public JsonSchemaOutputWriter() {
	}
	
	@Override
	public void saveJsonSchemaFiles(Map<String, JsonNode> schemas, File outputDirectory)
			throws JsonGenerationException, JsonMappingException, IOException {
		if (!outputDirectory.exists()) {
			log.info("Creating directory {}", outputDirectory.getAbsolutePath());
			outputDirectory.mkdirs();
		}
		for (Map.Entry<String, JsonNode> entry : schemas.entrySet()) {
			String fileName = entry.getKey().toLowerCase();
			JsonNode jsonSchemaFile = entry.getValue();
			File outputFile = new File(outputDirectory, fileName + JSON);
			ObjectMapper mapper = new ObjectMapper();
			mapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, jsonSchemaFile);
			log.info("Created json schema file {}", outputFile.getName());
		}

	}

}
