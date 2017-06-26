/*
 *
 * Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries European Union, 2015-2016.
 *
 * This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package eu.europa.ec.fisheries.uvms.plugins.fluxActivity.mapper;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

/**
 * Created by padhyad on 5/24/2017.
 */
public class FluxNamespaceMapper extends NamespacePrefixMapper {

    private static final String UDT_PREFIX = "udt"; // DEFAULT NAMESPACE
    private static final String UDT = "urn:un:unece:uncefact:data:standard:UnqualifiedDataType:20";

    private static final String RAM_PREFIX = "ram";
    private static final String RAM = "urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:20";

    private static final String RSM_PREFIX = "rsm";
    private static final String RSM = "urn:un:unece:uncefact:data:standard:FLUXResponseMessage:6";

    @Override
    public String getPreferredPrefix(String namespaceUri, String suggestion, boolean b) {
        if(UDT.equalsIgnoreCase(namespaceUri)) {
            return UDT_PREFIX;
        } else if(RAM.equalsIgnoreCase(namespaceUri)) {
            return RAM_PREFIX;
        } else if(RSM.equalsIgnoreCase(namespaceUri)) {
            return RSM_PREFIX;
        }
        return suggestion;
    }

    @Override
    public String[] getPreDeclaredNamespaceUris() {
        return new String[] { UDT, RAM, RSM};
    }
}
