package org.fenixedu.ulisboa.reports.domain.exceptions;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response.Status;

import org.fenixedu.bennu.core.domain.exceptions.DomainException;
import org.fenixedu.ulisboa.reports.util.ULisboaReportsUtil;

public class ULisboaReportsDomainException extends DomainException {

    private static final long serialVersionUID = 1L;

    public ULisboaReportsDomainException(String key, String... args) {
        super(ULisboaReportsUtil.BUNDLE, key, args);
    }

    public ULisboaReportsDomainException(Status status, String key, String... args) {
        super(status, ULisboaReportsUtil.BUNDLE, key, args);
    }

    public ULisboaReportsDomainException(Throwable cause, String key, String... args) {
        super(cause, ULisboaReportsUtil.BUNDLE, key, args);
    }

    public ULisboaReportsDomainException(Throwable cause, Status status, String key, String... args) {
        super(cause, status, ULisboaReportsUtil.BUNDLE, key, args);
    }

    public static void throwWhenDeleteBlocked(Collection<String> blockers) {
        if (!blockers.isEmpty()) {
            throw new ULisboaReportsDomainException("key.return.argument", blockers.stream().collect(Collectors.joining(", ")));
        }
    }

}
