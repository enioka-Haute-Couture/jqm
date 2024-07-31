FROM maven:3-eclipse-temurin-21 as installer

ARG MVN_SETTINGS=" "
ARG SKIP_TESTS=true

WORKDIR /jqm-all

RUN apt update && apt install unzip

#COPY ./docker/windows/nexus/settings.xml ./jqm-all ./  # FIXME: Nexus settings needed ?
COPY ./jqm-all ./

RUN --mount=type=cache,target=/root/.m2/repository mvn install -DskipTests=${SKIP_TESTS} ${MVN_SETTINGS}
RUN mkdir /jqm
RUN unzip ./jqm-service/target/jqm*.zip -d /tmp/
RUN mv /tmp/jqm*/ /tmp/jqm/

COPY ./kubernetes-dbless/docker/node.sh /tmp/jqm/bin/
COPY ./kubernetes-dbless/docker/selfConfig.standalone.xml /tmp/jqm/



FROM azul/zulu-openjdk-alpine:21

COPY --from=installer /tmp/jqm/ /jqm/
RUN apk add curl

ENV JAVA_OPTS="-Xms128m -Xmx512m -XX:MaxMetaspaceSize=128m" \
    JQM_ROOT="/jqm" \
    JQM_NODE_NAME="ContainerNode" \
    JQM_NODE_WS_INTERFACE="0.0.0.0" \
    JQM_INIT_MODE=SINGLE \
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
RUN java -jar jqm.jar Update-Schema
RUN java -jar jqm.jar Import-ClusterConfiguration -f selfConfig.standalone.xml
RUN java -jar jqm.jar Import-JobDef -f ./jobs/jqm-demo
RUN rm -f .\logs\*
RUN chmod 700 /jqm/bin/*.sh

# Run node on startup
ENTRYPOINT /jqm/bin/node.sh

STOPSIGNAL SIGINT
