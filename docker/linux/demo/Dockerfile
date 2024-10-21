# A test image with a custom driver.

ARG JQM_VERSION



FROM enioka/jqm:${JQM_VERSION}

ARG JQM_VERSION
RUN curl --silent https://jdbc.postgresql.org/download/postgresql-42.7.4.jar --output /jqm/ext/pgsql.jar
RUN curl --silent https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.33/mysql-connector-java-8.0.33.jar --output /jqm/ext/mysql.jar

LABEL com.enioka.description="JQM with the pgsql driver included based on version ${JQM_VERSION}"
