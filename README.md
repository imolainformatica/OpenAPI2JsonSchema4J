# OpenAPI2JsonSchema4J

Maven plugin that converts swagger 2.0/OAS 3.0.x schema objects into self contained json schema DRAFT_4 files for every request or response.

[![Java CI with Maven](https://github.com/imolainformatica/OpenAPI2JsonSchema4J/actions/workflows/maven.yml/badge.svg?branch=develop)](https://github.com/imolainformatica/OpenAPI2JsonSchema4J/actions/workflows/maven.yml)





### Interface requirements



- Each operation MUST have a non-empty OperationID field
- Each operation MUST not declare inline request/response body but always reference to Object Schema (no error is thrown, only a warn log)




### Notes about OAS 3.0.X conversion to DRAFT_4 Json Schema

* OAS `nullable` field adds `"null"` to `type` array if `nullable` is `true`
* OAS specific properties are deleted from JsonSchema output (discriminator, deprecated, xml, example,...)
* format attributes are maintened as-is





### Usage

**Maven goal**: generate

**Default lifecycle phase**: generate-sources

#### Configuration

| Parameter       | Description                                                  | Required |
| --------------- | ------------------------------------------------------------ | -------- |
| strict          | true / false, if "true" adds an additionalProperties = false field in complex object to avoid other json fields to appear | Y        |
| outputDirectory | json schema files are written here                           | Y        |
| interfaceFile   | path to swagger/oas file (in yaml or json format)            | Y        |



#### Example

```xml
<plugin>
						<groupId>it.imolinfo.maven.plugins</groupId>
						<artifactId>openapi2jsonschema4j</artifactId>
						<version>1.0.1</version>
						<configuration>
							<strict>true</strict>
						</configuration>
						<executions>
							<execution>
								<id>exec-id</id>
								<phase>generate-sources</phase>
								<configuration> 
									<outputDirectory>${basedir}/target/json-schema</outputDirectory>
									<interfaceFile>${basedir}/path/to/swagger.json</interfaceFile>
								</configuration>
								<goals>
									<goal>generate</goal>
								</goals>
							</execution>
						</executions>
					</plugin>
```