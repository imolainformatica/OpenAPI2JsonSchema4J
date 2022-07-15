package it.imolainformatica.openapi2jsonschema4j.impl;

import java.io.File;
import java.util.*;

import io.swagger.v3.oas.models.media.*;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.processors.syntax.SyntaxValidator;

import io.swagger.models.*;
import io.swagger.models.properties.*;
import it.imolainformatica.openapi2jsonschema4j.base.BaseJsonSchemaGenerator;
import it.imolainformatica.openapi2jsonschema4j.base.IJsonSchemaGenerator;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DraftV4JsonSchemaGenerator extends BaseJsonSchemaGenerator implements IJsonSchemaGenerator {

	private static final String SCHEMAS = "schemas";
	private static final String COMPONENTS = "components";
	private boolean strict;

	public DraftV4JsonSchemaGenerator(boolean strict) {
		this.strict = strict;
	}

	private Map<String, JsonNode> generateForObjects() throws Exception {
		for (String ref : getMessageObjects()) {
			String title = ref.replace(DEFINITIONS2, "");
			Map<String, Object> defs = (Map<String, Object>) ((HashMap<String, Schema>) getObjectsDefinitions()).clone();
			Schema<Object> ob = (Schema<Object>) defs.get(title);
			defs.remove(title);
			Map<String, Object> res = new HashMap<String, Object>();
			Map<String,Object> schemas = new HashMap<>();
			schemas.put(SCHEMAS,defs);
			res.put(COMPONENTS, schemas);
			res.put(TITLE2, title);
			log.info("Generating json schema for object '{}' of type {}", title,ob.getClass());
			if (ob instanceof ObjectSchema) {
				res.put(TYPE, ((ObjectSchema) ob).getType());
				res.put(PROPERTIES, ob.getProperties());
				res.put(REQUIRED,ob.getRequired());
				if (((ObjectSchema) ob).getAdditionalProperties()!=null) {
					log.info("additionalProperties already exists...setting to true in json schema {}",((ObjectSchema) ob).getAdditionalProperties());
					res.put(ADDITIONAL_PROPERTIES,true);
				} else {
					res.put(ADDITIONAL_PROPERTIES, !this.strict);
				}
			}
			if (ob instanceof ArraySchema) {
				res.put(ITEMS, ((ArraySchema) ob).getItems());
				res.put(TYPE, ((ArraySchema) ob).getType());
				res.put(MIN_ITEMS, ((ArraySchema) ob).getMinItems());
				res.put(MAX_ITEMS, ((ArraySchema) ob).getMaxItems());
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
					navigateProperty(name, (Schema) m.get(name), usedDefinition, res);
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
			for (Schema m : cm.getAllOf()) {
				navigateModel(m.get$ref(), usedDefinition,res,null);
			}

		} /* else if (ob instanceof RefModel) {
			RefModel rm = (RefModel)ob;
			Map<String, Property> m = rm.getProperties();
			log.debug("properties={}",m);
			if (m!=null) {
				for (String name : m.keySet()) {
					navigateProperty(name, m.get(name), usedDefinition, res);
				}
			}

		} */else {
			throw new RuntimeException(ob.getClass()+" not handled!");
		}
	}

	private void navigateProperty(String propertyName, Schema p, List<String> usedDefinition, Map<String, Object> res){
		log.debug("property name '{}' of type {}",propertyName,p);
		if (p.getClass() == Schema.class) {
			navigateModel(p.get$ref(),usedDefinition,res,null);
		} else if (p instanceof ArraySchema) {
			ArraySchema ap = (ArraySchema) p;
			log.debug("Array property={} items={}",ap,ap.getItems());
			navigateProperty("items",ap.getItems(),usedDefinition,res);
		} else if (p instanceof ObjectSchema){
			ObjectSchema op = (ObjectSchema) p;
			for (String name : op.getProperties().keySet()){
				navigateProperty(name,op.getProperties().get(name),usedDefinition,res);
			}
		} else if (p instanceof MapSchema) {
			MapSchema mp = (MapSchema)p;
			log.debug("additionalProperties={}",mp.getAdditionalProperties());
			if (mp.getAdditionalProperties() instanceof Schema) {
				navigateProperty(mp.getName(), (Schema)mp.getAdditionalProperties(), usedDefinition, res);
			}
		} else {
			log.debug(p.getClass() + " - nothing to do!");
		}
	}

	private JsonNode postprocess(Map<String, Object> res) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		SimpleBeanPropertyFilter theFilter = SimpleBeanPropertyFilter.serializeAllExcept(ORIGINAL_REF);
		FilterProvider filters = new SimpleFilterProvider().addFilter("myFilter", theFilter);
		String json = mapper.writer(filters).writeValueAsString(res);
		ObjectMapper mapper2 = new ObjectMapper();
		JsonNode jsonNode = mapper2.readValue(json, JsonNode.class);
		process("", jsonNode);
		if (isValidJsonSchemaSyntax(jsonNode)) {
			log.info("Valid json schema");
			return jsonNode;
		} else {
			throw new Exception("Invalid json Schema");
		}
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
			if (currentNode.get(TYPE) != null) {
				String type = currentNode.get(TYPE).asText();
				if ("object".equals(type)) {

					if (on.get(ADDITIONAL_PROPERTIES)!=null) {
						log.debug("already defined additionalProperties with value {}",on.get(ADDITIONAL_PROPERTIES).asText());
					} else {
						on.set(ADDITIONAL_PROPERTIES, BooleanNode.valueOf(!this.strict));
						log.debug("setting additional properties with value {}", !this.strict);
					}
				}
			}
			if (currentNode.get(ORIGINAL_REF) != null) {
				on.remove(ORIGINAL_REF);
				log.debug("removing originalRef field");
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


		Swagger sw = readFromInterface20(interfaceFile);
		Map<String, JsonNode> schemas = generateForObjects();
		return schemas;

	}


}
