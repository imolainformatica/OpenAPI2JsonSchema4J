package it.imolainformatica.openapi2jsonschema4j.main;

import java.io.File;

import com.beust.jcommander.Parameter;

import lombok.Getter;

@Getter
public class Args {


	@Parameter(names = {
			"--interface" }, description = "path to swagger file. Only 2.0 swagger supported (both yaml or json format)", converter = FileConverter.class, variableArity = true, required = true, order = 5)
	private File swagger;
	
	@Parameter(names = {
	"--output-dir" }, description = "path to output directory where json schema files will be created", converter = FileConverter.class, variableArity = true, required = true, order = 5)
	private File outputDir;
	
	@Parameter(names = {  "--strict" },description="Prohibits properties not in the schema (additionalProperties: false)")
	private boolean strict=true;

	
	@Parameter(names = { "-h", "--help" }, help = true)
	private boolean help;

}
