package it.imolainformatica.plugins.openapi2jsonschema4j;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import lombok.extern.slf4j.Slf4j;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;

@Slf4j
public class GenerateJsonSchemaMojoTest extends AbstractMojoTestCase {

	@Before
	public void setUp() throws Exception {
        // required for mojo lookups to work
        super.setUp();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		try {
	       File testPom = new File( getBasedir(),
	    	          "src/test/resources/maven-project/testOK-pom.xml" ); 
	        MavenProject project = new MavenProject();
	        project.setFile(testPom);
	        project.setModel(pomToModel(testPom));
			GenerateJsonSchemaMojo mojo = (GenerateJsonSchemaMojo) lookupConfiguredMojo(project, "generate");
			assertNotNull( mojo );
			mojo.execute();
		} catch (Exception e) {
			log.error("Eccezione ",e);
			fail("Eccezione non prevista");
		}  
	}
	
    private Model pomToModel(File pom) throws Exception {
        BufferedReader in = new BufferedReader(new FileReader(pom));
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read(in);
        return model;
    }

}
