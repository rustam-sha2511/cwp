package com.cwp.alice.controller;

import java.security.SecureRandom;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.cwp.alice.constants.GenericConstants;
import com.cwp.alice.model.CwUsers;
import com.cwp.alice.rs.request.dto.RequestRootObject;
import com.cwp.alice.rs.response.dto.ResponseRootObject;
import com.cwp.alice.service.CaseWorkerPortalService;
import com.google.gson.Gson;

import ai.api.AIServiceException;
import ai.api.model.AIResponse;

@RestController
public class DialogFlowConversationController extends AIServiceServlet{
	private static final Logger logger = Logger.getLogger(CaseWorkerPortalController.class);
	public static final Logger errorLogger = Logger.getLogger(GenericConstants.LOGGER_ERROR_NAME);
	
	@Autowired
	CaseWorkerPortalService cwpServices;
		
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
			
			//Check for fallback intent
			if(null == cwUsers || null == cwUsers.getSessionId() ||
					!cwUsers.getSessionId().equalsIgnoreCase(requestRootObject.getSessionId())) {
				responseRootObject.setSpeech("You are not authorized to interact with Alice. Please contact Administrator.");
				responseRootObject.setDisplayText("You are not authorized to interact with Alice. Please contact Administrator.");
				System.out.println("Setting fallback intent ...");
				requestRootObject.getResult().getMetadata().setIntentName("Default Fallback Intent");
			}
			
			//System.out.println("<======= Input Intent Name is :"+intentName);
			if(intentName.equalsIgnoreCase("UserAppointmentIntent")) {
				responseRootObject.setSpeech("Your next appointment is with Amit Kumar for Well-being session at 11:30 in Cafetaria for 30 minutes."
						+ "\nYou have one more follow up meeting for the day. For details, visit https://case-worker-portal-alice.7e14.starter-us-west-2.openshiftapps.com/CaseWorkerPortal/cwAppointment");
				responseRootObject.setDisplayText("this text is displayed visually");
			} else {			
				responseRootObject.setSpeech("this text is spoken out loud if the platform supports voice interactions");
				responseRootObject.setDisplayText("this text is displayed visually");
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
}
