# JQM react web app

## Install JQM instance for development

Build JQM from latest 3.x.x:

```bash
mvn package -DskipTests -Pbuild-frontend
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

## Reverse Proxy Deployment

The Web UI can be deployed behind a reverse proxy with a path prefix.

**Set the environment variable `VITE_BASE_PATH`** to the wanted path with a leading slash but no trailing slash. (e.g. `export VITE_BASE_PATH=/marsu/pi/lami`)

## Packaging

The frontend is not built by default to speed things up.

During development after making changes to the React app, include them in the build by running `yarn build` before `mvn package`.

For production builds use the `-Pbuild-frontend` option with `mvn package`.
