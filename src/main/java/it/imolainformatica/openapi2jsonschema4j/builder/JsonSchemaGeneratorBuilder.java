package it.imolainformatica.openapi2jsonschema4j.builder;

import it.imolainformatica.openapi2jsonschema4j.base.IJsonSchemaGenerator;
import it.imolainformatica.openapi2jsonschema4j.base.JsonSchemaVersion;
import it.imolainformatica.openapi2jsonschema4j.impl.DraftV4JsonSchemaGenerator;

public class JsonSchemaGeneratorBuilder {

	JsonSchemaVersion schemaVersion = JsonSchemaVersion.DRAFT_V4;
	Boolean strictGeneration = false;
	
	public JsonSchemaGeneratorBuilder withOutputSchemaVersion(JsonSchemaVersion schemaVersion) {
		this.schemaVersion=schemaVersion;
		return this;
	}
	
	public JsonSchemaGeneratorBuilder withStrictGeneration(Boolean strict) {
		this.strictGeneration=strict;
		return this;
	}
	
	public IJsonSchemaGenerator build() {
		if (schemaVersion==JsonSchemaVersion.DRAFT_V4) {
			return new DraftV4JsonSchemaGenerator(this.strictGeneration);
		}
		return null;
	}
}
