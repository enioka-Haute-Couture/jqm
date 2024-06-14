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

The legacy web console should now be accessible.

### API proxy configuration

If the web console port is different from `59977`, **set the environment variable `JQM_CONSOLE_PORT`** otherwise your local setup won't work.

## Start

* Use `yarn install` to install dependencies
* Use `yarn start`to start development server

## Troubleshooting

* If you get a white screen check the API calls being made ; 504 HTTP errors indicate the proxy is not properly configured ([see API proxy configuration](#api-proxy-configuration))
