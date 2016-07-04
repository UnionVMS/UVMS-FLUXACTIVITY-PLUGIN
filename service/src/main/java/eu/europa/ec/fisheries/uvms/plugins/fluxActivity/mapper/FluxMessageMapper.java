package eu.europa.ec.fisheries.uvms.plugins.fluxActivity.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sanera on 02/06/2016.
 */
public class FluxMessageMapper {

    private static Logger LOG = LoggerFactory.getLogger(FluxMessageMapper.class);

  /*  public static FLUXFAReportMessageType mapToExchangeFluxFaReportType(FLUXFAReportMessage fluxFAReportMessage)  {
        FLUXFAReportMessageType fluxFAReportMessageType = new FLUXFAReportMessageType();

        fluxFAReportMessageType.setFLUXReportDocument(convertFLUXReportDocumentToExchangeType(fluxFAReportMessage.getFLUXReportDocument()));
        return fluxFAReportMessageType;

    }

    private static FLUXReportDocumentType convertFLUXReportDocumentToExchangeType(FLUXReportDocument fluxReportDocument){

        FLUXReportDocumentType fluxReportDocumentType = new  FLUXReportDocumentType();
        fluxReportDocumentType.setTypeCode(fluxReportDocument.getTypeCode());
        fluxReportDocumentType.setCreationDateTime(fluxReportDocument.getCreationDateTime());
        FLUXPartyType fluxPartyType = new FLUXPartyType();

        fluxReportDocumentType.setOwnerFLUXParty( fluxReportDocument.getOwnerFLUXParty());

        return fluxReportDocumentType;

    }*/
}
