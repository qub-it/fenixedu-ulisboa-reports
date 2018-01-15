package org.fenixedu.bennu;

import org.fenixedu.bennu.spring.BennuSpringModule;

@BennuSpringModule(basePackages = "org.fenixedu.ulisboa.reports", bundles = "FenixeduULisboaReportsResources")
public class FenixeduULisboaReportsSpringConfiguration {
    public final static String BUNDLE = "resources/FenixeduULisboaReportsResources";
}
