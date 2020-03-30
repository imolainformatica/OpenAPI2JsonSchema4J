package it.imolainformatica.openapi2jsonschema4j.main;

import java.io.File;

import com.beust.jcommander.IStringConverter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileConverter implements IStringConverter<File> {
	public File convert(String file) {
		return new File(file);
	}
}
