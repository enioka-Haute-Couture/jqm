# JQM react web app

## Install JQM instance for development

Build JQM from latest 3.x.x:

```bash
mvn package -DskipTests
```

From the jqm-3.x.x-SNAPSHOT folder, run the following commands:

```bash
./jqm.sh createnode
java -jar jqm.jar Import-JobDef -f jobs/jqm-demo/xml/xml-jd-demo.xml
java -jar jqm.jar Set-WebConfiguration --change DISABLE_AUTHENTICATION
./jqm.sh startconsole
```

The legacy Web console should now be accessible.

Change the proxy port in package.json for requests to be properly redirected.

## Start

* Use `yarn install` to install dependencies
* Use `yarn start`to start development server

## Technical TODOs

* fix formatting in vscode, add settings
* add Roboto font ?
* 2 or 4 tab spaces
* https://webpack.js.org/guides/hot-module-replacement/
* Use absolute import only
* renable strict mode ?
