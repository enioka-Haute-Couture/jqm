FROM maven:3-jdk-8 as installer

ARG MVN_SETTINGS=" "
ARG SKIP_TESTS=true

WORKDIR /jqm-all

# Hack to keep libraries in a layer to speed up subsequent builds (as long as pom.xml files do not change)
#COPY ./jqm-all/**/pom.xml ./marsu/
#RUN find .
#RUN mvn dependency:go-offline

# Resume normal operations
COPY ./docker/windows/nexus/settings.xml ./jqm-all ./

RUN apt install unzip

RUN mvn install -DskipTests=${SKIP_TESTS} ${MVN_SETTINGS}
RUN mkdir /jqm
RUN unzip ./jqm-service/target/jqm*.zip -d /tmp/
RUN mv /tmp/jqm*/ /tmp/jqm/

COPY ./docker/linux/*.sh /tmp/jqm/bin/
COPY ./docker/config/selfConfig*.xml /tmp/jqm/



FROM openjdk:8-jre-alpine

COPY --from=installer /tmp/jqm/ /jqm/
RUN apk add curl

ENV JAVA_OPTS="-Xms128m -Xmx512m -XX:MaxMetaspaceSize=128m" \
    JQM_ROOT="/jqm" \
    JQM_NODE_NAME="ContainerNode" \
    JQM_NODE_WS_INTERFACE="0.0.0.0" \
    JQM_CREATE_NODE_IF_MISSING=0 \
    JQM_CREATE_NODE_TEMPLATE=TEMPLATE_WEB \
    JQM_POOL_CONNSTR="jdbc:hsqldb:file:db/jqmdatabase;shutdown=true;hsqldb.write_delay=false" \
    JQM_POOL_USER="sa" \
    JQM_POOL_PASSWORD="" \
    JQM_POOL_DRIVER="org.hsqldb.jdbcDriver" \
    JQM_POOL_VALIDATION_QUERY="SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS" \
    JQM_POOL_INIT_SQL=\
    JQM_POOL_MAX=10 \
    JQM_HEALTHCHECK_URL="http://localhost:1789/ws/simple/localnode/health"

EXPOSE 1789 1790 1791
VOLUME /jqm/hotdeploy/ \
    /jqm/ext/drivers/

WORKDIR /jqm

# Import initial config
RUN java -jar jqm.jar -u ; java -jar jqm.jar -c selfConfig.single.xml ; java -jar jqm.jar -importjobdef ./jobs/jqm-demo ; rm -f .\logs\*

# Run node on startup
ENTRYPOINT /jqm/bin/node.sh

# Healthcheck is equivalent to calling Node.AllPollersPolling
HEALTHCHECK --interval=30s --start-period=91s --retries=2 --timeout=10s CMD curl --connect-timeout 2 -q --http1.1 -4 -s -S  ${JQM_HEALTHCHECK_URL}
STOPSIGNAL SIGINT
