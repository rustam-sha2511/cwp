package com.cwp.alice.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.cwp.alice.constants.GenericConstants;
import com.cwp.alice.model.CwAppointments;
import com.cwp.alice.model.CwCases;
import com.cwp.alice.model.CwUsers;
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
			
			//User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			CwUsers cwUsers = cwpServices.findCaseWorkerById(
					Integer.valueOf(requestRootObject.getResult().getParameters().getUser_id()));
			
			System.out.println("uSER OBJ: " + cwUsers.toString());
			System.out.println("Users Session ID is :"+cwUsers.getSessionId());
			System.out.println("Request Root Object Session ID is :"+requestRootObject.getSessionId());
			
			//Check for fallback intent
			if(null == cwUsers || null == cwUsers.getSessionId() || null == requestRootObject.getSessionId() ||
					!cwUsers.getSessionId().equalsIgnoreCase(requestRootObject.getSessionId())) {
				responseRootObject.setSpeech("You are not authorized to interact with Alice. Please contact Administrator.");
				responseRootObject.setDisplayText("You are not authorized to interact with Alice. Please contact Administrator.");
				System.out.println("Setting fallback intent ...");
				requestRootObject.getResult().getMetadata().setIntentName("Default Fallback Intent");
				responseRootObject.setSource("cws.openshift.com");
				return responseRootObject;
			}
			
			//Business Case: 4
			if(intentName.equalsIgnoreCase("CaseValidationIntent")) {
				String inputCaseId = "100"; //TO-DO Get case id from ALICE window
				CwCases cwCase = dfcServices.getCaseByCaseId(inputCaseId);
				
				if (null != cwCase && cwCase.getCwId() != cwUsers.getCwId()) {
					String responseOut = "You do not have permission to open the case " + cwCase.getCwId()
							+ ". Please reach out to case owner " + cwCase.getAssignedCwName() + " at email " + cwUsers.getEmail();
					
					responseRootObject.setSpeech(responseOut);
					responseRootObject.setDisplayText(responseOut);
				}
			}
			
			//Business Case: 5
			if(intentName.equalsIgnoreCase("UserAppointmentIntent")) {
				List<CwAppointments> cwAppointments = dfcServices.getCwAppointments(cwUsers.getCwId().toString());
				
				if (!cwAppointments.isEmpty()) {
					StringBuilder sb = new StringBuilder();
					sb.append("You have " + cwAppointments.size() + " pending for today.");

					for (int i = 0; i < cwAppointments.size(); i++) {
						sb.append("\nAppointment" + (i + 1) + " is with " + cwAppointments.get(i).getOrganizer() + " for " + cwAppointments.get(i).getSubject() + 
								" at " + cwAppointments.get(i).getTime() + " in " + cwAppointments.get(i).getLocation() + " for " + cwAppointments.get(i).getDuration());
					}
					
					sb.append("\nFor details, visit the My Appointments page. "
							+ "Link: https://case-worker-portal-alice.7e14.starter-us-west-2.openshiftapps.com/CaseWorkerPortal/cwAppointment");
					responseRootObject.setSpeech(sb.toString());
					responseRootObject.setDisplayText(sb.toString());
				} else {
					responseRootObject.setSpeech("You do not have any appointments for the day.");
					responseRootObject.setDisplayText("You do not have any appointments for the day.");
				}
				
			}
			
			//Business Case: 6
			if(intentName.equalsIgnoreCase("CaseCreationIntent")) {
				String hohName = "Test User"; //TO-DO Get this dynamically from ALICE
				String noOfAdults = "2";
				String noOfChildren = "2";
				String monthlyIncome = "200";
				String responseOut = dfcServices.createNewCase(cwUsers, hohName, noOfAdults, noOfChildren, monthlyIncome);
				
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
			if(intentName.equalsIgnoreCase("AppLogoutIntent")) {
				
			}
			
			
			responseRootObject.setSource("cws.openshift.com");
			
			//System.out.println("<========= Output JSON is :"
					//+ gson.toJson(responseRootObject) + "================>");
			logger.error("Successfully created object");
			
		} catch (Exception e) {
			errorLogger.error("Classname: CwpDashboardController. Error in Case Creation: " + e);
			e.printStackTrace();
			logger.error("Error in Case Creation: " + e.getMessage());
			responseRootObject.setSpeech("You are not authorized to interact with Alice. Please contact Administrator.");
			responseRootObject.setDisplayText("You are not authorized to interact with Alice. Please contact Administrator.");
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
			//System.out.println("Returning Speech "+response);
		} catch (AIServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	    return response;
	}
	
	public static void main(String[] args) {
		List<String> test = new ArrayList<>();
		test.add("W");
		test.add("E");
		
		System.out.println(test.size());
	}
}
