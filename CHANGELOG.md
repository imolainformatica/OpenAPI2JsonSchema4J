# CHANGELOG

1.0.7 - 30/05/2024
- fix bug while handling generation of oneOf construct inside json schema
- fix issue preventing generation of schemas in case of objects without properties, forcing generation with additionalProperties enabled
- fix issue preventing generation of json schemas with swagger files that have objects schemas defined inline rather than inside "components"

1.0.6 - 07/07/2023
- updated swagger-parser library due to a bug that could not read additionalProperties inside a model.

1.0.5 - 13/04/2023
- removed additional "jsonSchema" field from json schema files when reading a swagger 2.0 file

1.0.4 - 02/03/2023
- fix bug handling additionalProperties if already defined with false value

1.0.3 - 01/03/2023
- fix issues regarding handling additionalProperties 

1.0.2 - 18/07/2022
- support for oas 3.0.X conversion to json schema draft4

1.0.1 - 28/04/2022

- removed unused definition in json schema file for each request/response
- issues/4 - fixed npe
