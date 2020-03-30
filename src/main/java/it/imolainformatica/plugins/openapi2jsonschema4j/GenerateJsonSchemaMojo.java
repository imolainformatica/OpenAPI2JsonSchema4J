package it.imolainformatica.plugins.openapi2jsonschema4j;

import java.io.File;
import java.util.Map;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.fasterxml.jackson.databind.JsonNode;

import it.imolainformatica.openapi2jsonschema4j.base.IJsonSchemaGenerator;
import it.imolainformatica.openapi2jsonschema4j.base.IJsonSchemaOutputWriter;
import it.imolainformatica.openapi2jsonschema4j.base.JsonSchemaVersion;
import it.imolainformatica.openapi2jsonschema4j.builder.JsonSchemaGeneratorBuilder;
import it.imolainformatica.openapi2jsonschema4j.impl.JsonSchemaOutputWriter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class GenerateJsonSchemaMojo extends AbstractMojo {

    @Parameter(defaultValue = "${localRepository}", readonly = true, required = true)
    private ArtifactRepository localRepository;
    @Parameter(property = "interface.file", required = true)
    private String interfaceFile;
    @Parameter(property = "output.dir", required = true)
    private String outputDirectory = null;
    @Parameter(property = "strict", required = true)
    private Boolean strict = true;

    public void execute() throws MojoExecutionException {
        try {
        	File swagger = new File(interfaceFile);
        	if (!swagger.exists() || !swagger.canRead() ) {
        		throw new Exception("The file "+interfaceFile+" doesn't exists or cannot be read!");
        	}
        	log.info("Input interface file = {}",swagger.toString());
        	File outputDir = null;
        	if (outputDirectory==null) {
        		outputDir = swagger.getAbsoluteFile().getParentFile();
        		log.info("Default output directory set to {}",outputDir);
        	} else {
        		outputDir = new File(outputDirectory);
        		log.info("Output directory = {}",outputDir);
        	}
       	
			IJsonSchemaGenerator jsg = new JsonSchemaGeneratorBuilder()
					.withOutputSchemaVersion(JsonSchemaVersion.DRAFT_V4).withStrictGeneration(strict).build();
			Map<String, JsonNode> schemas = jsg.generate(swagger);
			IJsonSchemaOutputWriter writer = new JsonSchemaOutputWriter();
			writer.saveJsonSchemaFiles(schemas, outputDir);
        } catch (Exception e) {
            log.debug(e.getMessage());
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }
}
