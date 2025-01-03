package com.example.routeoptilib.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.moveinsync.models.IdentificationDocument;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Cabby {
	
	private String id;
	
	private String name;
	
	private Date dateOfBirth;
	
	private String vendorId;
	
	private String currentCity;
	
	private String mobileNumber;
	
	private IdentificationDocument licence;
	
	private String address;
	
	private String currentAddress;
	
	private IdentificationDocument badge;
	
	private IdentificationDocument alternateGovernmentID;
	
	private String alternateMobileNumber;
	
	private Date backgroundStatusUpdateTime;
	
	private String backgroundDocumentURl;
	
	private String driverImageUrl;
	
	private Boolean status;
	
	private Boolean blacklistStatus;
	
	private String gender;
	
	private String comment;
	
	@DateTimeFormat(pattern = "dd/MM/yyyy")
	private Date bgvExpiryDate;
	
	private String policeVerificationStatus;
	
	@DateTimeFormat(pattern = "dd/MM/yyyy")
	private Date policeVerificationExpiryDate;
	
	private IdentificationDocument medicalIdentification;
	
	private IdentificationDocument trainingIdentification;
	
	private IdentificationDocument louIdentification;
	
	private IdentificationDocument currentAddIdentification;
	
	private IdentificationDocument inductionFormIdentification;
	
	private Date inductionDate;
	
	private String identifier;
	
	private IdentificationDocument eyeTestIdentification;
	
	private IdentificationDocument vaccinationIdentification;
	
	private List<String> officeIds;
	
	private String fatherName;
	
}