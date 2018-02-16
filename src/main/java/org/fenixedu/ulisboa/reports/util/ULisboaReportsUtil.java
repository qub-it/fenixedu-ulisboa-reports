/**
 * This file was created by Quorum Born IT <http://www.qub-it.com/> and its 
 * copyright terms are bind to the legal agreement regulating the FenixEdu@ULisboa 
 * software development project between Quorum Born IT and Serviços Partilhados da
 * Universidade de Lisboa:
 *  - Copyright © 2015 Quorum Born IT (until any Go-Live phase)
 *  - Copyright © 2015 Universidade de Lisboa (after any Go-Live phase)
 *
 *
 * 
 * This file is part of FenixEdu ULisboa Reports
 *
 * FenixEdu Treasury is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu Treasury is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu Treasury.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fenixedu.ulisboa.reports.util;

import org.fenixedu.bennu.FenixeduULisboaReportsSpringConfiguration;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.commons.i18n.LocalizedString;

public class ULisboaReportsUtil {

    public static final String BUNDLE = FenixeduULisboaReportsSpringConfiguration.BUNDLE.replace('/', '.');

    public static final String PREFIX = "label.org.fenixedu.ulisboa.reports.";

    // @formatter: off
    /**********
     * BUNDLE *
     **********/
    // @formatter: on

    public static String bundle(final String key, final String... args) {
        return BundleUtil.getString(ULisboaReportsUtil.BUNDLE, PREFIX + key, args);
    }

    public static LocalizedString bundleI18N(final String key, final String... args) {
        return BundleUtil.getLocalizedString(ULisboaReportsUtil.BUNDLE, PREFIX + key, args);
    }

}