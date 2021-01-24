@org.osgi.annotation.bundle.Export
@org.osgi.annotation.versioning.Version("3.0.0")
@org.osgi.annotation.bundle.Header(name = "Required-Capability", value = "osgi.serviceloader; filter:=\"(osgi.serviceloader=org.eclipse.jetty.util.security.CredentialProvider)\";cardinality:=multiple, osgi.serviceloader; filter:=\"(osgi.serviceloader=org.eclipse.jetty.http.HttpFieldPreEncoder)\";cardinality:=multiple, osgi.serviceloader; filter:=\"(osgi.serviceloader=org.eclipse.jetty.security.Authenticator$Factory)\";cardinality:=multiple, osgi.serviceloader; filter:=\"(osgi.serviceloader=org.eclipse.jetty.xml.ConfigurationProcessorFactory)\";cardinality:=multiple, osgi.extender; filter:=\"(osgi.extender=osgi.serviceloader.processor)\"")
package com.enioka.jqm.service;
