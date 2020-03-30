package it.imolainformatica.openapi2jsonschema4j.base;
import java.io.File;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AbstractIT {


	protected File loadFromResourceFile(String resourceName) {
		log.debug("loading resource {}",resourceName);
		File file = new File(
				getClass().getClassLoader().getResource(resourceName).getFile()
			);
		return file;
		
	}
	
}
