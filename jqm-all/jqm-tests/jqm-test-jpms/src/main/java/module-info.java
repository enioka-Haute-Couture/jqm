module com.enioka.jqm.tests.jpms
{
    ///////////////////////////////////////////
    // For payload with JQM API test
    requires com.enioka.jqm.payload.api;

    exports com.enioka.jqm.tests.jpms;

    ///////////////////////////////////////////
    // For service test

    // API interface definition
    exports com.enioka.jqm.tests.jpms.api;

    // We expose an implementation of the API
    provides com.enioka.jqm.tests.jpms.api.TestService with com.enioka.jqm.tests.jpms.services.TestServiceImpl1;

    // And we use the service inside the JQM payload
    uses com.enioka.jqm.tests.jpms.api.TestService;
}
