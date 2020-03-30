package it.imolainformatica.openapi2jsonschema4j.main;

import java.util.Map;

import com.beust.jcommander.JCommander;
import com.fasterxml.jackson.databind.JsonNode;

import it.imolainformatica.openapi2jsonschema4j.base.IJsonSchemaGenerator;
import it.imolainformatica.openapi2jsonschema4j.base.IJsonSchemaOutputWriter;
import it.imolainformatica.openapi2jsonschema4j.base.JsonSchemaVersion;
import it.imolainformatica.openapi2jsonschema4j.builder.JsonSchemaGeneratorBuilder;
import it.imolainformatica.openapi2jsonschema4j.impl.JsonSchemaOutputWriter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {

	public static void main(String[] argv) throws Exception {
		Args args = new Args();
		JCommander jct = JCommander.newBuilder().addObject(args).build();

		jct.parse(argv);

		if (args.isHelp()) {
			jct.usage();
			return;
		}
		try {
			IJsonSchemaGenerator jsg = new JsonSchemaGeneratorBuilder()
					.withOutputSchemaVersion(JsonSchemaVersion.DRAFT_V4).withStrictGeneration(args.isStrict()).build();
			Map<String, JsonNode> schemas = jsg.generate(args.getSwagger());
			IJsonSchemaOutputWriter writer = new JsonSchemaOutputWriter();
			writer.saveJsonSchemaFiles(schemas, args.getOutputDir());
		} catch (Exception e) {
			log.error("Unexpected exception " + e.getMessage(), e);
			System.exit(1);
		}
		System.exit(0);
	}

}
