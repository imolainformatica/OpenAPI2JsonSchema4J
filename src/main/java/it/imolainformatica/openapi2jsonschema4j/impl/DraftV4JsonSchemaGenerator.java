package it.imolainformatica.openapi2jsonschema4j.impl;

import java.io.File;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.datatype.jsr310.*;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.*;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.processors.syntax.SyntaxValidator;

import it.imolainformatica.openapi2jsonschema4j.base.BaseJsonSchemaGenerator;
import it.imolainformatica.openapi2jsonschema4j.base.IJsonSchemaGenerator;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DraftV4JsonSchemaGenerator extends BaseJsonSchemaGenerator implements IJsonSchemaGenerator {

	private static final String SCHEMAS = "schemas";
	private static final String COMPONENTS = "components";
	private static final String EXAMPLESETFLAG = "exampleSetFlag";
	public static final String EXAMPLE = "example";
	public static final String XML = "xml";
	public static final String EXTENSIONS = "extensions";
	private static final String NULLABLE = "nullable";
	private static final String DISCRIMINATOR = "discriminator";
	private static final String READONLY = "readOnly";
	private static final String WRITEONLY = "writeOnly";
	private static final String EXTERNALDOCS = "externalDocs";
	private static final String DEPRECATED = "deprecated";

	private static final String ALLOF = "anyOf";
	private static final String ONEOF = "oneOf";
	private static final String ANYOF = "anyOf";
	

	private static final String JSONSCHEMA = "jsonSchema";

	private static final String TYPES = "types";
	private static final String[] ignoreProperties = {ORIGINAL_REF,
			EXAMPLESETFLAG,
			EXAMPLE,
			XML,
			EXTENSIONS,
			NULLABLE,
			DISCRIMINATOR,
			READONLY,
			WRITEONLY,
			EXTERNALDOCS,
			DEPRECATED,
			JSONSCHEMA,
			TYPES};
	private static final List<String> ignorePropertiesList = Arrays.asList(ignoreProperties);
	public static final String NULL = "null";
	private boolean strict;


	public DraftV4JsonSchemaGenerator(boolean strict) {
		this.strict = strict;
	}

	private Map<String, JsonNode> generateForObjects() throws Exception {
		for (String ref : getMessageObjects().keySet()) {
			String title = ref.replace(DEFINITIONS2, "");
			Map<String, Object> defs = (Map<String, Object>) ((HashMap<String, Schema>) getObjectsDefinitions()).clone();			
			Schema<Object> ob = (Schema<Object>) defs.get(title);
			defs.remove(title);
			Map<String, Object> res = new HashMap<String, Object>();
			Map<String,Object> schemas = new HashMap<>();
			schemas.put(SCHEMAS,defs);
			res.put(COMPONENTS, schemas);
			if (isSwaggerFlattened) {
				title = getMessageObjects().get(ref);
			}
			res.put(TITLE2, title);		
			log.info("Generating json schema for object '{}' of type {}", title,ob.getClass());
			if (ob instanceof ObjectSchema) {
				res.put(TYPE, ((ObjectSchema) ob).getType());
				res.put(PROPERTIES, ob.getProperties());
				res.put(REQUIRED, ob.getRequired());

				if (ob.getProperties() == null || ob.getProperties().isEmpty()) {
					res.put(PROPERTIES, new HashMap<String, Schema>());
					log.info("Object '{}' has no properties, creating empty properties object.");
					res.put(ADDITIONAL_PROPERTIES, true);
					log.info("FORCED ADDITIONALPROPERTIES TO TRUE");
				} else {
					res.put(ADDITIONAL_PROPERTIES, ((ObjectSchema) ob).getAdditionalProperties() != null);
				}
			}
			if (ob instanceof ComposedSchema) {
				res.put(TYPE, ((ComposedSchema) ob).getType());
				res.put(ALLOF, ob.getAllOf());
				res.put(ONEOF, ob.getOneOf());				
				res.put(ANYOF, ob.getAnyOf());
			}
			if (ob instanceof ArraySchema) {
				Schema<?> items = ob.getItems();
				if (items instanceof ObjectSchema && items.getProperties() == null) {
					log.info("Array items of type object has no properties");
				}
				res.put(ITEMS, ((ArraySchema) ob).getItems());
				res.put(TYPE, ((ArraySchema) ob).getType());
				res.put(MIN_ITEMS, ((ArraySchema) ob).getMinItems());
				res.put(MAX_ITEMS, ((ArraySchema) ob).getMaxItems());
			}
			if (ob instanceof MapSchema) {
				res.put(PROPERTIES, ((MapSchema) ob).getProperties());
				res.put(TYPE, ((MapSchema) ob).getType());
				res.put(REQUIRED, ob.getRequired());
				res.put(ADDITIONAL_PROPERTIES, ((MapSchema) ob).getAdditionalProperties());
			}
			res.put($SCHEMA, HTTP_JSON_SCHEMA_ORG_DRAFT_04_SCHEMA);
			removeUnusedObject(res,ob);
			getGeneratedObjects().put(title, postprocess(res));
		}
		return getGeneratedObjects();
	}

	private void removeUnusedObject(Map<String, Object> res, Schema<Object> ob) {
		log.info("Removing unused definition for '{}'",res.get(TITLE2));
		List<String> usedDefinition = new ArrayList<>();
		navigateModel((String)res.get(TITLE2),usedDefinition,res,ob);
		log.info("Used Object = {}",usedDefinition);
		List<String> tbdeleted = new ArrayList<>();
		for (String key : ((Map<String,Object>)((Map<String,Object>)res.get(COMPONENTS)).get(SCHEMAS)).keySet()) {
			if (!usedDefinition.contains(key)){
				log.debug("Removing definition for object {}",key);
				tbdeleted.add(key);
			} else {
				log.debug("Object {} is used!",key);
			}
		}
		for (String del : tbdeleted){
			((Map<String,Object>)((Map<String,Object>)res.get(COMPONENTS)).get(SCHEMAS)).remove(del);
		}
	}

	private void navigateModel(String originalRef, List<String> usedDefinition, Map<String, Object> res, Object ob) {
		log.debug("Analyzing ref {} object={}",originalRef,ob);
		String objectName=originalRef.replace(DEFINITIONS2,"");
		if (ob==null){
			ob = ((Map<String,Object>)((Map<String,Object>)res.get(COMPONENTS)).get(SCHEMAS)).get(objectName);
		}
		log.debug("Analyzing object {} {}",ob,originalRef);
		if (usedDefinition.contains(objectName)){
			log.info("Found circular reference for object {}!",objectName);
			return;
		}
		usedDefinition.add(objectName);
		if (ob instanceof ObjectSchema) {
			ObjectSchema mi = (ObjectSchema)ob;
			Map<String, Schema> m = mi.getProperties();
			log.debug("properties={}",m);
			if (m!=null) {
				for (String name : m.keySet()) {
					navigateSchema(name, (Schema) m.get(name), usedDefinition, res);
				}
			}
		} else if (ob instanceof ArraySchema) {
			log.debug("array model={}",res.get(ITEMS));
			if (res.get(ITEMS) instanceof Schema) {
				Schema s = (Schema) res.get(ITEMS);
				navigateModel(s.get$ref(),usedDefinition,res,null);
			}
		} else if (ob instanceof ComposedSchema) {
			ComposedSchema cm = (ComposedSchema)ob;
			lookComposedModel(cm.getAllOf(),usedDefinition,res);
			lookComposedModel(cm.getAnyOf(),usedDefinition,res);
			lookComposedModel(cm.getOneOf(),usedDefinition,res);
			if (cm.getNot()!=null) {
				navigateModel(cm.getNot().get$ref(), usedDefinition, res, null);
			}
		} else if  (ob instanceof MapSchema) {
			MapSchema ms = (MapSchema)ob;
			Map<String, Schema> m = ms.getProperties();
			log.debug("map schema properties={}",m);
			if (m!=null) {
				for (String name : m.keySet()) {
					navigateSchema(name, (Schema) m.get(name), usedDefinition, res);
				}
			}
			if (ms.getAdditionalProperties() instanceof Schema) {
				navigateSchema("", (Schema)ms.getAdditionalProperties(), usedDefinition, res);
			}

		} else if (ob instanceof Schema && ((Schema)ob).get$ref()!=null){
			navigateModel(((Schema)ob).get$ref(),usedDefinition,res,null);
		}
	}

	private void lookComposedModel(List<Schema> schema, List<String> usedDefinition, Map<String, Object> res) {
		if (schema!=null)
			for (Schema m : schema) {
				navigateModel(m.get$ref(), usedDefinition,res,null);
			}
	}

	private void navigateSchema(String propertyName, Schema p, List<String> usedDefinition, Map<String, Object> res){
		log.debug("property name '{}' of type {}",propertyName,p);
		if (p.getClass() == Schema.class) {
			if (p.get$ref()!=null)
				navigateModel(p.get$ref(),usedDefinition,res,null);
		} else if (p instanceof ArraySchema) {
			ArraySchema ap = (ArraySchema) p;
			log.debug("Array property={} items={}",ap,ap.getItems());
			if(ap.getItems() instanceof ObjectSchema && ap.getItems().getProperties() == null ){
				log.info("Array items of type object has no properties");
			}
			navigateSchema("items",ap.getItems(),usedDefinition,res);
		} else if (p instanceof ObjectSchema){
			ObjectSchema op = (ObjectSchema) p;
			if(op.getProperties() != null) {
				for (String name : op.getProperties().keySet()) {
					navigateSchema(name, op.getProperties().get(name), usedDefinition, res);
				}
			}
		} else if (p instanceof MapSchema) {
			MapSchema mp = (MapSchema)p;
			log.debug("additionalProperties={}",mp.getAdditionalProperties());
			if (mp.getAdditionalProperties() instanceof Schema ) {
				navigateSchema(mp.getName(), (Schema)mp.getAdditionalProperties(), usedDefinition, res);
			}
		} else {
			log.debug(p.getClass() + " - nothing to do!");
		}
	}

	@JsonFilter("myFilter")
	public class DynamicMixIn {
	}

	private JsonNode postprocess(Map<String, Object> res) throws Exception {
		//need to handle all nullable oas3 possible values
		res = handleNullableFields(res);
		JsonNode jsonNode = removeNonJsonSchemaProperties(res);
		process("", jsonNode);
		if (isValidJsonSchemaSyntax(jsonNode)) {
			log.info("Valid json schema");
			return jsonNode;
		} else {
			throw new Exception("Invalid json Schema");
		}
	}

	private JsonNode removeNonJsonSchemaProperties(Map<String, Object> res) throws JsonProcessingException {

		iterateMap(res,null);
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		String json = mapper.writeValueAsString(res);
		ObjectMapper mapper2 = new ObjectMapper();
		JsonNode jsonNode = mapper2.readValue(json, JsonNode.class);
		return jsonNode;
	}

	//this method remove all unmanaged oas3 json schema props
	private void iterateMap(Map<String, Object> res, String father) {
		if (res==null)
			return;
		for (String k : res.keySet()) {
			if (res.get(k)!=null) {
				log.debug("key={}   father={}",k,father);
				if (!"properties".equals(father)) {
					//devo rimuovere i valori da ignorare (solo se il padre non è un campo 'properties'
					if (ignorePropertiesList.contains(k)) {
						log.debug("annullo la chiave {}",k);
						res.put(k, null);
					}
				}
				//altrimenti non faccio nulla
				if (res.get(k) instanceof Map) {
					iterateMap((Map<String, Object>) res.get(k), k);
				}
			}
		}

	}




	private Map<String, Object> handleNullableFields(Map<String, Object> result) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		Map<String, Object> res = mapper.convertValue(result, new TypeReference<Map<String, Object>>(){});
		getAllKeys(res);
		return res;
	}

	private void process(String prefix, JsonNode currentNode) {
		if (currentNode.isArray()) {
			ArrayNode arrayNode = (ArrayNode) currentNode;
			Iterator<JsonNode> node = arrayNode.elements();
			int index = 1;
			while (node.hasNext()) {
				process(!prefix.isEmpty() ? prefix + "-" + index : String.valueOf(index), node.next());
				index += 1;
			}
		} else if (currentNode.isObject()) {
			currentNode.fields().forEachRemaining(entry -> process(
					!prefix.isEmpty() ? prefix + "-" + entry.getKey() : entry.getKey(), entry.getValue()));
			ObjectNode on = ((ObjectNode) currentNode);
			if (currentNode.has(TYPE) && "object".equals(currentNode.get(TYPE).asText())) {
				if (!currentNode.has(PROPERTIES) || !currentNode.get(PROPERTIES).fields().hasNext()) {
					on.put(ADDITIONAL_PROPERTIES, true);
					log.debug("setting additional properties with value {}", true);
				}
			}
			if (currentNode.has(ORIGINAL_REF)) {
				on.remove(ORIGINAL_REF);
				log.debug("removing originalRef field");
			}
			if (currentNode.get(EXAMPLESETFLAG) != null) {
				on.remove(EXAMPLESETFLAG);
				log.debug("removing exampleSetFlag field");
			}
		} else {
			log.debug(prefix + ": " + currentNode.toString());
		}
	}




	private boolean isValidJsonSchemaSyntax(JsonNode jsonSchemaFile) {
		SyntaxValidator synValidator = JsonSchemaFactory.byDefault().getSyntaxValidator();
		ProcessingReport report = synValidator.validateSchema(jsonSchemaFile);
		log.debug("report={}", report);
		return report.isSuccess();

	}

	@Override
	public Map<String, JsonNode> generate(File interfaceFile) throws Exception {
		readFromInterface(interfaceFile);
		return generateForObjects();
	}

	private void getAllKeys(Map<String, Object> jsonElements) {
		Boolean nullable = false;

		for (Map.Entry entry : jsonElements.entrySet()) {
			log.debug("entry {}",entry.getKey());
			if (entry.getKey().equals("nullable")){
				if (entry.getValue() instanceof Boolean) {
					if (((Boolean)entry.getValue())==Boolean.TRUE){
						nullable = true;
					}
				}
			}
			if (entry.getValue() instanceof Map) {
				Map<String, Object> map = (Map<String, Object>) entry.getValue();
				getAllKeys(map);
			} else if (entry.getValue() instanceof List) {
				List<?> list = (List<?>) entry.getValue();
				list.forEach(listEntry -> {
					if (listEntry instanceof Map) {
						Map<String, Object> map = (Map<String, Object>) listEntry;
						getAllKeys(map);
					}
				});
			}
		}
		if (Boolean.TRUE.equals(nullable)) {
			String actualType = (String) jsonElements.get(TYPE);
			jsonElements.put(TYPE, new String[]{actualType, NULL});
		}
	}


}
