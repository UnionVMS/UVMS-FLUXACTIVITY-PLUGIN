HOW TO POST TO THE WEB SERVICE OF THE PLUGIN :

You can use a tool like postman

1. URL : {{hostname}}:8080/unionvms/activity-service/FLUXFAReportMessageService/FLUXFAReportMessageReceiverBean?wsdl
2. Example message :

<?xml version="1.0" encoding="UTF-8"?>
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:urn="urn:xeu:bridge-connector:v1">
   <soapenv:Header />
   <soapenv:Body>
      <urn:Connector2BridgeRequest FR="EST" ON="BLA1234BLAONCEAGAIN1234BLA999" AD="XEU" DF="urn:un:unece:uncefact:fisheries:FLUX:MDM:EU:2" AR="True" TO="2" USER="FLUX" GUID="DEC3A0ED-E2B0-489B-903D-D79313787E2E">
         <rsm:FLUXFAReportMessage xsi:schemaLocation="urn:un:unece:uncefact:data:standard:FLUXFAReportMessage:3 xsd/FLUXFAReportMessage_3p1/FLUXFAReportMessage_3p1.xsd" xmlns:ram="urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:20" xmlns:udt="urn:un:unece:uncefact:data:standard:UnqualifiedDataType:20" xmlns:rsm="urn:un:unece:uncefact:data:standard:FLUXFAReportMessage:3" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
         <rsm:FLUXReportDocument>
            <ram:ID schemeID="UUID">932ef896-9382-42b1-8f11-3d7276d829a2</ram:ID>
            <ram:CreationDateTime>
               <udt:DateTime>2018-10-17T11:56:56.979Z</udt:DateTime>
            </ram:CreationDateTime>
            <ram:PurposeCode listID="FLUX_GP_PURPOSE">9</ram:PurposeCode>
            <ram:Purpose>FA-L00-00-0074_003_Negative_Error returned when FA Report Document with VesselTransportMeans/SpecifiedContactParty/SpecifiedContactPerson/FamilyName, VesselTransportMeans/specifiedcontactparty/specifiedcontactperson/alias Family name missing</ram:Purpose>
            <ram:OwnerFLUXParty>
               <ram:ID schemeID="FLUX_GP_PARTY">SRC</ram:ID>
            </ram:OwnerFLUXParty>
         </rsm:FLUXReportDocument>
         <rsm:FAReportDocument>
            <ram:TypeCode listID="FLUX_FA_REPORT_TYPE">DECLARATION</ram:TypeCode>
            <ram:AcceptanceDateTime>
               <udt:DateTime>2018-10-17T06:56:56.979Z</udt:DateTime>
            </ram:AcceptanceDateTime>
            <ram:RelatedFLUXReportDocument>
               <ram:ID schemeID="UUID">504be0f3-3776-4bfc-a5ad-d40292c9fa9e</ram:ID>
               <ram:CreationDateTime>
                  <udt:DateTime>2018-10-17T08:56:56.979Z</udt:DateTime>
               </ram:CreationDateTime>
               <ram:PurposeCode listID="FLUX_GP_PURPOSE">9</ram:PurposeCode>
               <ram:OwnerFLUXParty>
                  <ram:ID schemeID="FLUX_GP_PARTY">SRC</ram:ID>
               </ram:OwnerFLUXParty>
            </ram:RelatedFLUXReportDocument>
            <ram:SpecifiedFishingActivity>
               <ram:TypeCode listID="FLUX_FA_TYPE">DISCARD</ram:TypeCode>
               <ram:OccurrenceDateTime>
                  <udt:DateTime>2018-10-17T04:56:56.979Z</udt:DateTime>
               </ram:OccurrenceDateTime>
               <ram:ReasonCode listID="FA_REASON_DISCARD">BAI</ram:ReasonCode>
               <ram:VesselRelatedActivityCode listID="VESSEL_ACTIVITY">ANC</ram:VesselRelatedActivityCode>
               <ram:OperationsQuantity unitCode="C62">1</ram:OperationsQuantity>
               <ram:SpecifiedFACatch>
                  <ram:SpeciesCode listID="FAO_SPECIES">COD</ram:SpeciesCode>
                  <ram:WeightMeasure unitCode="KGM">52.65</ram:WeightMeasure>
                  <ram:TypeCode listID="FA_CATCH_TYPE">DISCARDED</ram:TypeCode>
                  <ram:SpecifiedSizeDistribution>
                     <ram:ClassCode listID="FISH_SIZE_CLASS">LSC</ram:ClassCode>
                  </ram:SpecifiedSizeDistribution>
                  <ram:AppliedAAPProcess>
                     <ram:TypeCode listID="FISH_PRESENTATION">GUT</ram:TypeCode>
                     <ram:TypeCode listID="FISH_PRESERVATION">FRE</ram:TypeCode>
                     <ram:ConversionFactorNumeric>1</ram:ConversionFactorNumeric>
                     <ram:ResultAAPProduct>
                        <ram:WeightMeasure unitCode="KGM">45</ram:WeightMeasure>
                        <ram:PackagingUnitQuantity unitCode="C62">1</ram:PackagingUnitQuantity>
                        <ram:PackagingTypeCode listID="FISH_PACKAGING">BOX</ram:PackagingTypeCode>
                        <ram:PackagingUnitAverageWeightMeasure unitCode="KGM">1</ram:PackagingUnitAverageWeightMeasure>
                     </ram:ResultAAPProduct>
                  </ram:AppliedAAPProcess>
               </ram:SpecifiedFACatch>
			<ram:RelatedFLUXLocation>
			<ram:TypeCode listID="FLUX_LOCATION_TYPE">POSITION</ram:TypeCode>
				<ram:SpecifiedPhysicalFLUXGeographicalCoordinate>
					<ram:LongitudeMeasure>-14.156</ram:LongitudeMeasure>
					<ram:LatitudeMeasure>46.758</ram:LatitudeMeasure>
				</ram:SpecifiedPhysicalFLUXGeographicalCoordinate>
			</ram:RelatedFLUXLocation>
               <ram:SpecifiedFishingGear>
                  <ram:TypeCode listID="GEAR_TYPE">PS</ram:TypeCode>
                  <ram:RoleCode listID="FA_GEAR_ROLE">ONBOARD</ram:RoleCode>
                  <ram:ApplicableGearCharacteristic>
                     <ram:TypeCode listID="FA_GEAR_CHARACTERISTIC">ME</ram:TypeCode>
                     <ram:ValueMeasure unitCode="MMT">140</ram:ValueMeasure>
                  </ram:ApplicableGearCharacteristic>
                  <ram:ApplicableGearCharacteristic>
                     <ram:TypeCode listID="FA_GEAR_CHARACTERISTIC">GM</ram:TypeCode>
                     <ram:ValueMeasure unitCode="MTR">100</ram:ValueMeasure>
                  </ram:ApplicableGearCharacteristic>
                  <ram:ApplicableGearCharacteristic>
                     <ram:TypeCode listID="FA_GEAR_CHARACTERISTIC">HE</ram:TypeCode>
                     <ram:ValueMeasure unitCode="MTR">100</ram:ValueMeasure>
                  </ram:ApplicableGearCharacteristic>
               </ram:SpecifiedFishingGear>
               <ram:SpecifiedFishingTrip>
                  <ram:ID schemeID="EU_TRIP_ID">SRC-TRP-TTT20181017145656979</ram:ID>
                  <ram:TypeCode listID="FISHING_TRIP_TYPE">JFO</ram:TypeCode>
               </ram:SpecifiedFishingTrip>
            </ram:SpecifiedFishingActivity>
            <ram:SpecifiedVesselTransportMeans>
               <ram:ID schemeID="CFR">CYP123456789</ram:ID>
               <ram:Name>GOLF</ram:Name>
               <ram:RoleCode listID="FA_VESSEL_ROLE">PAIR_FISHING_PARTNER</ram:RoleCode>
               <ram:RegistrationVesselCountry>
                  <ram:ID schemeID="TERRITORY">CYP</ram:ID>
               </ram:RegistrationVesselCountry>
               <ram:SpecifiedContactParty>
                  <ram:RoleCode listID="FLUX_CONTACT_ROLE">MASTER</ram:RoleCode>
                  <ram:SpecifiedStructuredAddress>
                     <ram:StreetName>ll</ram:StreetName>
                     <ram:CityName>CABOURG</ram:CityName>
                     <ram:CountryID schemeID="TERRITORY">XEU</ram:CountryID>
                     <ram:PlotIdentification>17</ram:PlotIdentification>
                     <ram:PostalArea> 14390</ram:PostalArea>
                  </ram:SpecifiedStructuredAddress>
                  <ram:SpecifiedContactPerson>
                  </ram:SpecifiedContactPerson>
               </ram:SpecifiedContactParty>
            </ram:SpecifiedVesselTransportMeans>
         </rsm:FAReportDocument>
      </rsm:FLUXFAReportMessage>
      </urn:Connector2BridgeRequest>
   </soapenv:Body>
</soapenv:Envelope>