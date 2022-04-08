# A test image with a custom driver.

ARG JQM_VERSION



FROM enioka/jqm:${JQM_VERSION}

ARG JQM_VERSION
RUN curl --silent https://jdbc.postgresql.org/download/postgresql-42.3.3.jar --output /jqm/ext/pgsql.jar

LABEL com.enioka.description="JQM with the pgsql driver included based on version ${JQM_VERSION}"
