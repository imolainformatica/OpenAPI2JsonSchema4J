# FrontendAPIDefinitionAdapter

[TOC]

Questo progetto contiene la logica che permette a unire le informazioni proveniente dai file yaml di definizione delle frontend API con dei template infrastrutturali permettendo di generare un file json importabile sull'api manager tramite lo strumento [Axway https://github.com/Axway-API-Management-Plus/apimanager-swagger-promote ]

 

### Compilazione

Tramite il comando maven:

> mvn clean package

### Output

nella cartella target viene gereato un'archivio frontend-api-definition-adapter-[versione]-dist.zip

Scompattare il contenuto del file zip.

Struttura della cartella

- **conf**
  - **environment.properties** (contiene tutte le configurazioni di tutti gli ambienti)
  - **logback.xml** (configurazione di logging)
- **jar**
  - **frontend-api-definition-adapter-[versione]-jar-with-dependencies.jar** (contiene la logica di esecuzione)
- **log** (cartella che contiene tutti i log di esecuzione)
- **frontend-api-definition-adapter.sh**  (script di lancio)



### Esecuzione

lo script a linea di comando prende in input i seguenti parametri:

```reStructuredText
  Options:
  * --env
      ambiente target del rilascio: test_integrato, collaudo, produzione
  * -c, --config
      percorso del file di configurazione che contiene i dettagli
      dell'ambiente
  * --api-list
      file che contiene la lista di api da rilasciare
    --copy-dependant-objects
      specificare se gli oggetti dipendenti devono essere copiati anch'essi
      oppure semplicemente referenziati tramite un path relativo
      Default: false
    -h, --help

  * --output-dir
      percorso della cartella dove vengono salvati i file json di output
  * --template-base-path
      percorso di base relativamente ai percorsi dei template presenti dentro
      al file api.list
  * --yaml-base-path
      percorso di base relativamente ai percorsi dei file yaml presenti dentro
      al file api.list
```
### Formato file lista API

la lista di api da trasformare deve seguire il seguente formato: 

percorso relativo del file yaml della frontend API e una lista (almeno uno) di percorsi relativi ai template infrastrutturali da applicare separati da punto e virgola.

Esempio:

```reStructuredText
APSD2_CBIAccountInformationAPI_v3/Resources/manager/CBIAccountInformationAPI_v3.yaml;Templates/mutuaAutenticazione.json
APSD2_CBICardAccountInformationAPI_v3/Resources/manager/CBICardAccountInformationAPI_v3.yaml;Templates/mutuaAutenticazione.json;Templates/mascheramento.json
```

Questi percorsi relativi vengono risolti considerando i parametri **yaml-base-path** e **template-base-path** che devono essere passati come parametri di input.

### Formato file per rilascio API

I file che contengono la definizione della backend api e frontend api da rilasciare devono seguire un determinato formato in sintassi yaml. 

Attualmente supporta le seguenti tipologie di importazione di una backend API:

- API REST con importazione da swagger file caricato da file system
- Servizi SOAP con importazione wsdl da url 


```yaml
name: <NOME API>
version: <VERSIONE API>
organization: <NOME ORGANIZZAZIONE - deve essere già censita sul manager>
#per i servizi soap la descrizione non viene presa automaticamente dal wsdl
description: <descrizione generica del servizio>
# tipo di importazione: valori ammissibili: swagger (da file), wsdl (da url)
importType: <swagger | wsdl> 
#percorso del file swagger: da configurare solo se importType=swagger
swaggerLocation: <percorso relativo al file swagger da importare>
#url per recuperare il wsdl da importare: da configurare solo se importType=wsdl
wsdlURL: <url del wsdl>
resourcePath: <path di esposizione sul manager> es. /apsd2/GestioneConsensoService/v1
#Non modificare gli attributi "group", sono fissi per tutte le api
tags:
  - group: Service Scope
    value: <valore dell'attributo Service Scope>
  - group: Functional Domain
    value: <valore dell'attributo Functional Domain>
  - group: API Category
    value: <valore dell'attributo API Category>
  - group: Service Organization Owner
    value: <valore dell'attributo Service Organization Owner>
  - group: Service Application Owner
    value: <valore dell'attributo Service Application Owner>  
backendServiceURL: <url di puntamento al backend (senza context) es. http://localhost:8103
```


Lo script crea una backend ed una frontend api in stato unpublished con i parametri descritti nel file yaml. Per la parte di sicurezza inbound viene specificata una policy "**Two way SSL**" che può essere cambiata prima di essere pubblicata.

Gli attributi **wsdlUrl** e **backendServiceUrl**  consentono di posizionare dei placeholder (identificati dalla sintassi **${**nomeproprietà**}**) dentro i relativi valori con dei nomi di proprietà che **<u>devono essere presenti dentro al file environment.properties</u>**.

**Esempio**

se nel file yaml è presente 

```yaml
...
wsdlURL: ${channel.gateway.base.url}/apsd2/GestioneConsensoService/v1
...
```
e nel file environment.properties
```properties
channel.gateway.base.url=http://10.209.2.70
```
allora a runtime il valore dell'attributo wsdlURL verrà risolto in http://10.209.2.70/apsd2/GestioneConsensoService/v1

se non è presente allora effettua un'altra ricerca <u>antemponendo al nome della property l'ambiente</u> passato in input.

**Esempio**

se nel file yaml è presente 

```yaml
...
wsdlURL: ${channel.gateway.base.url}/apsd2/GestioneConsensoService/v1
...
```

e nel file environment.properties

```properties
sviluppo.channel.gateway.base.url=http://10.209.2.70
```

allora a runtime il valore dell'attributo wsdlURL verrà ugualmente risolto in http://10.209.2.70/apsd2/GestioneConsensoService/v1 

Se neanche in questo modo riesce a trovare il valore da sostituire allora viene sollevato un'eccezione e il programma termina. 