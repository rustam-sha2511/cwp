package com.cwp.alice.controller;

import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.cwp.alice.constants.GenericConstants;
import com.cwp.alice.dto.AliceConversationDetails;
import com.cwp.alice.model.CaseStatus;
import com.cwp.alice.model.CwAppointments;
import com.cwp.alice.model.CwCases;
import com.cwp.alice.model.CwUsers;
import com.cwp.alice.rs.request.dto.Context;
import com.cwp.alice.rs.request.dto.RequestRootObject;
import com.cwp.alice.rs.response.dto.ResponseRootObject;
import com.cwp.alice.service.CaseWorkerPortalService;
import com.cwp.alice.service.DialogFlowConversationService;
import com.google.gson.Gson;

import ai.api.AIServiceException;
import ai.api.model.AIResponse;

@RestController
public class DialogFlowConversationController extends AIServiceServlet{
	private static final Logger logger = Logger.getLogger(CaseWorkerPortalController.class);
	public static final Logger errorLogger = Logger.getLogger(GenericConstants.LOGGER_ERROR_NAME);
	
	@Autowired
	CaseWorkerPortalService cwpServices;
	
	@Autowired
	DialogFlowConversationService dfcServices;
		
	@RequestMapping(value = GenericConstants.URL_CONV_ALICE, method = { RequestMethod.POST }, 
			consumes = MediaType.APPLICATION_JSON_VALUE)
	public String showMyDashboardPOST(Model model, HttpSession session, 
				@RequestBody List<AliceConversationDetails> aliceConversationDetails) {
		try {
			session.setAttribute("aliceConversationDetails", aliceConversationDetails);
		} catch (Exception e) {
			errorLogger.error("Classname: CwpDashboardController. Error in saving alice conversation: " + e);
			logger.error("Error in saving alice conversation: " + e);
			model.addAttribute("message",
					"Error in saving alice conversation. Please contact helpdesk for assistance. " + e);
	
			return GenericConstants.PAGE_ERROR;
		}
	
		return "success";
	}
	
	@RequestMapping(value = GenericConstants.DIALOGFLOW_CONVERSATION_WEBHOOK, method = RequestMethod.POST,
			consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseRootObject processCaseCreation(@RequestBody RequestRootObject requestRootObject) {
		ResponseRootObject responseRootObject = new ResponseRootObject();
		System.out.println("<======= ============== Entering processCaseCreation method ================== ========>");
		
		Gson gson = new Gson();
		System.out.println("<========= Input JSON is :"
				+ gson.toJson(requestRootObject) + "================>");

		try {
			String intentName = requestRootObject.getResult().getMetadata().getIntentName();
			String userId = null;
			String cwPwd = null;
			String caseId = null;
			String newStatus = null;
			String createCasePaName = null;
			String monthlyIncome = null;
			String childrenCount = null;
			String adultCount = null;
			
			//Added for ALICE V2.0
			String zipcode = null;
			String county = null;
			String dob = null;
			String sex = null;
			String tobaccoUsage = null;
			String householdIncome = null;
			String frequency = null;
			String relationship = null;
			
			for(Context contextObj: requestRootObject.getResult().getContexts()) {
				if(contextObj.getName().equalsIgnoreCase("usercontext")) {
					userId = contextObj.getParameters().getUser_id();
					cwPwd = contextObj.getParameters().getCw_pwd();
					createCasePaName = contextObj.getParameters().getPa_name();
					monthlyIncome = contextObj.getParameters().getMonthly_income();
					childrenCount = contextObj.getParameters().getChildren_count();
					adultCount = contextObj.getParameters().getAdult_count();
					caseId = contextObj.getParameters().getCase_id();
					newStatus = contextObj.getParameters().getNew_status();
					//Added for ALICE V2.0
					zipcode = contextObj.getParameters().getZipcode();
					county = contextObj.getParameters().getCounty();
					dob = contextObj.getParameters().getDob();
					sex = contextObj.getParameters().getSex();
					tobaccoUsage = contextObj.getParameters().getTobaccoUsage();
					householdIncome = contextObj.getParameters().getHouseholdIncome();
					frequency = contextObj.getParameters().getFrequency();
					relationship = contextObj.getParameters().getRelationship();
					break;
				}
			}
			
			
			//User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			CwUsers cwUsers = cwpServices.findCaseWorkerById(Integer.valueOf(userId));
			
			System.out.println("uSER OBJ: " + cwUsers.toString());
			System.out.println("Users Session ID is :"+cwUsers.getSessionId());
			System.out.println("Request Root Object Session ID is :"+requestRootObject.getSessionId());
			
			//Check for fallback intent
			/*if(null == cwUsers || null == cwUsers.getSessionId() || null == requestRootObject.getSessionId() ||
					!cwUsers.getSessionId().equalsIgnoreCase(requestRootObject.getSessionId())) {
				responseRootObject.setSpeech("You are not authorized to interact with Alice. Please contact Administrator.");
				responseRootObject.setDisplayText("You are not authorized to interact with Alice. Please contact Administrator.");
				System.out.println("Setting fallback intent ...");
				requestRootObject.getResult().getMetadata().setIntentName("Default Fallback Intent");
				responseRootObject.setSource("cws.openshift.com");
				return responseRootObject;
			}*/
			
			//Business Case: 1
			if(intentName.equalsIgnoreCase("UserCaseSearchIntent")) {
				String inputCaseId = requestRootObject.getResult().getParameters().getCase_id();
				CwCases cwCase = dfcServices.getCaseByCaseId(inputCaseId);
				
				if (null != cwCase && cwCase.getCwId() != cwUsers.getCwId()) {
					String responseOut = "You do not have permission to open the case " + inputCaseId
							+ ". Please reach out to case owner " + cwCase.getAssignedCwName() + " at email " + cwUsers.getEmail();
					
					responseRootObject.setSpeech(responseOut);
					responseRootObject.setDisplayText(responseOut);
				} else {
					//String responseOut = "filtering results for case "+inputCaseId;
					String responseOut = "Filtering the table for case "+inputCaseId;
			
					responseRootObject.setSpeech(responseOut);
					responseRootObject.setDisplayText(responseOut);
				}
			}
			
			//Business Case: 2
			if(intentName.equalsIgnoreCase("CaseStatusIntent")) {
				String inputCaseId = requestRootObject.getResult().getParameters().getCase_id();
				CwCases cwCase = dfcServices.getCaseByCaseId(inputCaseId);
				String responseOut = null;
				
				if (null != cwCase && cwCase.getCwId() > 0) {
					if (cwCase.getCwId() != cwUsers.getCwId()) {
						responseOut = "You do not have permission to open the case " + inputCaseId
								+ ". Please reach out to case owner " + cwCase.getAssignedCwName() + " at email " + cwUsers.getEmail();
					} else {
						responseOut = "Status of case "+inputCaseId+" is "+cwCase.getStatus()+".";
					}
				} else {
					responseOut = "I cannot find any case status with the provided case ID.";
				}
				responseRootObject.setSpeech(responseOut);
				responseRootObject.setDisplayText(responseOut);
			}
			
			//Business Case: 3 & 4
			if(intentName.equalsIgnoreCase("CaseOpenIntent")) {
				String inputCaseId = requestRootObject.getResult().getParameters().getCase_id();
				CwCases cwCase = dfcServices.getCaseByCaseId(inputCaseId);
				
				if (null != cwCase && cwCase.getCwId() != cwUsers.getCwId()) {
					String responseOut = "You do not have permission to open the case " + inputCaseId
							+ ". Please reach out to case owner " + cwCase.getAssignedCwName() + " at email " + cwUsers.getEmail();
					
					responseRootObject.setSpeech(responseOut);
					responseRootObject.setDisplayText(responseOut);
				} else {
					//String responseOut = "displaying case "+inputCaseId;
					String responseOut = "Opening details for case "+inputCaseId;
			
					responseRootObject.setSpeech(responseOut);
					responseRootObject.setDisplayText(responseOut);
				}
			}
			
			//Business Case: 5
			if(intentName.equalsIgnoreCase("UserAppointmentIntent")) {
				List<CwAppointments> cwAppointments = dfcServices.getCwAppointments(cwUsers.getCwId().toString());
				
				if (!cwAppointments.isEmpty()) {
					StringBuilder sb = new StringBuilder();
					sb.append("You have " + cwAppointments.size() + " appointments pending for today.");

					for (int i = 0; i < cwAppointments.size(); i++) {
						sb.append("\nAppointment " + (i + 1) + " is with " + cwAppointments.get(i).getOrganizer() + " at " + cwAppointments.get(i).getTime() + ".");
					}
					
					sb.append("\nFor details, visit the My Appointments page.");
					responseRootObject.setSpeech(sb.toString());
					responseRootObject.setDisplayText(sb.toString());
				} else {
					responseRootObject.setSpeech("You do not have any appointments for the day.");
					responseRootObject.setDisplayText("You do not have any appointments for the day.");
				}
				
			}
			
			//Business Case: 6
			if(intentName.equalsIgnoreCase("CaseCreationIntent")) {
				/*String hohName = createCasePaName; //TO-DO Get this dynamically from ALICE
				String noOfAdults = adultCount;
				String noOfChildren = childrenCount;*/
				//String monthlyIncome = monthlyIncome;
				//String responseOut = dfcServices.createNewCase(cwUsers, createCasePaName, adultCount, childrenCount, monthlyIncome);
				String responseOut = "You are about to create a case for "+createCasePaName+" having "
						+ adultCount+" adults and "+childrenCount+" children in family with monthly income of "+monthlyIncome+". "
						+ "Do you agree with the details of case.";
				responseRootObject.setSpeech(responseOut);
				responseRootObject.setDisplayText(responseOut);
			}
			
			if(intentName.equalsIgnoreCase("CaseCreationIntent - yes")) {
				String responseOut = dfcServices.createNewCase(cwUsers, createCasePaName, adultCount, childrenCount, monthlyIncome);
				
				responseRootObject.setSpeech(responseOut);
				responseRootObject.setDisplayText(responseOut);
			}
			
			if(intentName.equalsIgnoreCase("CaseCreationIntent - no")) {
				String responseOut = "Case creation cancelled. Let me know if you change your mind.";
				responseRootObject.setSpeech(responseOut);
				responseRootObject.setDisplayText(responseOut);
			}
					
			
			//Business Case: 7
			if(intentName.equalsIgnoreCase("UpdateAccountIntent")) {
				String updateFieldType = "Email"; //TO-DO Get which field to update. 1 for Email. 2 for Role. 3 for Designation. 4 for Department
				String updatedFieldValue = "testNew@gmail.com";
				String responseOut = dfcServices.updateAccountDetails(cwUsers, updateFieldType, updatedFieldValue);
				
				responseRootObject.setSpeech(responseOut);
				responseRootObject.setDisplayText(responseOut);
			}
			
			//Business Case: 8
			//Logout scenario
			if(intentName.equalsIgnoreCase("AppLogoutIntent")) {
				responseRootObject.setSpeech("It was great working with you. See you next time!");
				responseRootObject.setDisplayText("It was great working with you. See you next time!");	
			} else if(intentName.equalsIgnoreCase("DisplayOwnerCaseIntent")){
				//Business Case: 9
				//responseRootObject.setSpeech("showing cases assigned to you");
				//responseRootObject.setDisplayText("showing cases assigned to "+cwUsers.getName());
				responseRootObject.setSpeech("Filtering the table to show all your cases ");
				responseRootObject.setDisplayText("Filtering the table to show all your cases "+cwUsers.getName());
			} else if(intentName.equalsIgnoreCase("UpdateCaseStatusIntent")){
				//Business Case: 10
				CwCases cwCase = cwpServices.getCaseByCaseId(caseId);
				System.out.println("Matched case is: "+cwCase.toString());
				String responseOut = null;
				if (null != cwCase && cwCase.getCwId() != cwUsers.getCwId()) {
					responseOut = "You do not have permission to update the case " + caseId
							+ ". Please reach out to case owner " + cwCase.getAssignedCwName() + " at email " + cwUsers.getEmail();
					
				} else {				
					if(!newStatus.equalsIgnoreCase(CaseStatus.APPROVED.value())
							&& !newStatus.equalsIgnoreCase(CaseStatus.DENIED.value())) {
						responseOut = "It seems you have not provided a valid status. Status can only be changed to either APPROVED or DENIED.";
						requestRootObject.getResult().getMetadata().setIntentName("Default Fallback Intent");
					} else {
						responseOut = dfcServices.updateCaseStatus(cwCase, newStatus);
					}
				}
				
				responseRootObject.setSpeech(responseOut);
				responseRootObject.setDisplayText(responseOut);
			} else if(intentName.equalsIgnoreCase("AnnonPlanSearchIntent")){
				//Business Case: 11
				System.out.println(">>> Matched Intent AnnonPlanSearchIntent");
				System.out.println("Zipcode is : " + zipcode);
				//TODO: To be changed to dynamic value
				String speechText = "Added member successfully. Do you want to add new household member?";
				/*String displayText = "Filtering out plans for below details:IN_STARTZipCode:98002IN_PCounty:KINGIN_PDOB:09/09/1991"+
						"IN_PSex:MaleIN_PTobacco Usage:NoIN_PHousehold Income:$200IN_PFrequency:AnnualyHH_STARTDOB:01/01/2001"+
						"IN_PSex:FemaleIN_PTobacco Usage:NoIN_PRelationship:SpouseHH_CHDOB:01/01/2001IN_PSex:Female"+
						"IN_PTobacco Usage:NoIN_PRelationship:Spouse";*/
				String displayText = "Added member with details:IN_STARTZipCode:"+zipcode+"IN_PCounty:"+county+"IN_PDOB:"+dob+
						"IN_PSex:"+sex+"IN_PTobacco Usage:"+tobaccoUsage+"IN_PHousehold Income:$"+householdIncome+"IN_PFrequency:"+frequency+
						"IN_PRelationship:"+relationship+"IN_ENDDo you want to add new household member?";
				responseRootObject.setSpeech(speechText);
				responseRootObject.setDisplayText(displayText);
			} else if(intentName.equalsIgnoreCase("AnnonPlanSearchIntent - yes")){
				//Business Case: 11
				System.out.println(">>> Matched Intent AnnonPlanSearchIntent - yes");
				//TODO: To be changed to dynamic value
				String speechText = "Household member added with date of birth as " + dob +". Do you want to add another household member?";
				/*String displayText = "Filtering out plans for below details:IN_STARTZipCode:98002IN_PCounty:KINGIN_PDOB:09/09/1991"+
						"IN_PSex:MaleIN_PTobacco Usage:NoIN_PHousehold Income:$200IN_PFrequency:AnnualyHH_STARTDOB:01/01/2001"+
						"IN_PSex:FemaleIN_PTobacco Usage:NoIN_PRelationship:SpouseHH_CHDOB:01/01/2001IN_PSex:Female"+
						"IN_PTobacco Usage:NoIN_PRelationship:Spouse";*/
				String displayText = "Added member with details:IN_STARTZipCode:"+null+"IN_PCounty:"+null+"IN_PDOB:"+dob+
						"IN_PSex:"+sex+"IN_PTobacco Usage:"+tobaccoUsage+"IN_PHousehold Income:$"+null+"IN_PFrequency:"+null+
						"IN_PRelationship:"+relationship+"IN_ENDDo you want to add new household member?";
				responseRootObject.setSpeech(speechText);
				responseRootObject.setDisplayText(displayText);
			} else if(intentName.equalsIgnoreCase("Default Fallback Intent")){
				//Business Case: 12
				System.out.println(">>> Matched Intent Default Fallback Intent");
				String inputText = requestRootObject.getResult().getResolvedQuery();
				System.out.println(">>> Fallback Intent text is : " + inputText);
				String speechText = "Can you say that again?";
				String displayText = "Can you say that again?";
				RestTemplate restTemplate = new RestTemplate();
				List<LinkedHashMap> responses = 
						restTemplate.getForObject("https://faq.wahealthplanfinder.org/search/faq/en/"+inputText+"?_format=json", List.class);
				StringBuffer displayTextToBeCreated = new StringBuffer();
				StringBuffer speechTextToBeCreated = new StringBuffer();
				if(responses.size()>0) {
					displayTextToBeCreated.append("Based on your input, here are the suggestions for you. "
							+ "Tap on the card or speak 'Select option ' along with number to view the details.");
					speechTextToBeCreated.append("Based on your input, here are the suggestions for you. "
							+ "Tap on the card or speak 'Select option ' along with number to view the details.");
					displayTextToBeCreated.append("FAQ_START");
					int responseCount = 1;
					for(LinkedHashMap resp: responses) {
						System.out.println("Valid response is : "+resp.get("title"));
						displayTextToBeCreated.append(resp.get("title"));
						speechTextToBeCreated.append("Select option " + responseCount + " for " + resp.get("title") + ".");
						displayTextToBeCreated.append("FAQ_BREAK");
						displayTextToBeCreated.append(resp.get("link"));
						displayTextToBeCreated.append("FAQ_END");
						responseCount++;
					}
					System.out.println(">>> Display text is : " + displayTextToBeCreated);
					System.out.println(">>> Speech text is : " + speechTextToBeCreated);
					displayText = displayTextToBeCreated.toString();
					speechText = speechTextToBeCreated.toString();
				}
				
				responseRootObject.setSpeech(speechText);
				responseRootObject.setDisplayText(displayText);
			}
						
			
			responseRootObject.setSource("cws.openshift.com");
			
			//System.out.println("<========= Output JSON is :"
					//+ gson.toJson(responseRootObject) + "================>");
			logger.error("Successfully created object");
			
		} catch (Exception e) {
			errorLogger.error("Classname: CwpDashboardController. Error in Case Creation: " + e);
			e.printStackTrace();
			logger.error("Error in Case Creation: " + e.getMessage());
			responseRootObject.setSpeech("I am facing some technical difficulties right now to perform this action for you. Please contact Administrator.");
			responseRootObject.setDisplayText("I am facing some technical difficulties right now to perform this action for you. Please contact Administrator.");
			requestRootObject.getResult().getMetadata().setIntentName("Default Fallback Intent");
			responseRootObject.setSource("cws.openshift.com");
		}
		
		return responseRootObject;
	}
	
	@RequestMapping(value = "/ai", method = RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody String processDialogFlowRequest(HttpServletRequest request, HttpSession session) {
		//ResponseRootObject responseRootObject = new ResponseRootObject();
		AIResponse aiResponse = new AIResponse();
		String response = "";
		try {
			//System.out.println("Val is: "+request.getParameter(DialogFlowConversationController.PARAM_API_AI_KEY));
			String secretKey = String.valueOf(session.getAttribute("aliceSecretKey"));
			System.out.println("Alice Secret key is : "+secretKey);
			aiResponse = request(request.getParameter("query"), secretKey);
			Gson gson = new Gson();
			response = gson.toJson(aiResponse);
			//response = aiResponse.getResult().getFulfillment().getSpeech();
			System.out.println("Returning Speech "+response);
		} catch (AIServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*String requestValue = null;
		String speechText = "I didn't get that. Could you please repeat that for me?";
		String displayText = "I didn't get that. Could you please repeat that for me?";
		if(null != request.getParameter("query")) {
			requestValue = request.getParameter("query");
		}
		
		System.out.println("Request Value is : " + requestValue);
		if(null != requestValue) {
			if(requestValue.equalsIgnoreCase("Log in with 101 / abcd")) {
				speechText = "Hi! This is Alice. How may I help you?";
				displayText = "Hi! This is Alice. How may I help you?";
			} else if(requestValue.equalsIgnoreCase("Search for a health plan")){
				speechText = "We will require some information to help you find a suitable plan for you and your family. "
						+ "Can you please help us with your zipcode?";
				displayText = "We will require some information to help you find a suitable plan for you and your family. "
						+ "Can you please help us with your zipcode?";
			} else if(requestValue.equalsIgnoreCase("Search for a plan")){
				speechText = "Filtering out plans for 2 household members with household income as $200 residing in CLARK.";
				displayText = "Filtering out plans for below details:IN_STARTZipCode:98002IN_PCounty:KINGIN_PDOB:09/09/1991"+
						"IN_PSex:MaleIN_PTobacco Usage:NoIN_PHousehold Income:$200IN_PFrequency:AnnualyHH_STARTDOB:01/01/2001"+
						"IN_PSex:FemaleIN_PTobacco Usage:NoIN_PRelationship:SpouseHH_CHDOB:01/01/2001IN_PSex:Female"+
						"IN_PTobacco Usage:NoIN_PRelationship:Spouse";
			} else {
				RestTemplate restTemplate = new RestTemplate();
				String encoded = "";
				try {
					encoded = UriUtils.encode(requestValue, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				List<LinkedHashMap> responses = 
						restTemplate.getForObject("https://faq.wahealthplanfinder.org/search/faq/en/"+encoded+"?_format=json", List.class);
				StringBuffer speechTextToBeCreated = new StringBuffer();
				if(responses.size()>0) {
					speechTextToBeCreated.append("Based on your input, here are the suggestions for you. Tap on the card to view the deatils.");
					speechTextToBeCreated.append("FAQ_START");
					for(LinkedHashMap resp: responses) {
						System.out.println("Valid response is : "+resp.get("title"));
						speechTextToBeCreated.append(resp.get("title"));
						speechTextToBeCreated.append("FAQ_BREAK");
						speechTextToBeCreated.append(resp.get("link"));
						speechTextToBeCreated.append("FAQ_END");
					}
					displayText = speechTextToBeCreated.toString();
				}				
			}
		}
		
		Result result = new Result();
		Fulfillment fulfillment = new Fulfillment();
		fulfillment.setSpeech(speechText);
		fulfillment.setDisplayText(displayText);
		result.setFulfillment(fulfillment);
		aiResponse.setResult(result);
		
		Gson gson = new Gson();
		response = gson.toJson(aiResponse);*/
	    return response;
	}
	
}