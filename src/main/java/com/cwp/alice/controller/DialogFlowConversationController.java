package com.cwp.alice.controller;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.cwp.alice.constants.GenericConstants;
import com.cwp.alice.rs.request.dto.RequestRootObject;
import com.cwp.alice.rs.response.dto.ContextOut;
import com.cwp.alice.rs.response.dto.Data;
import com.cwp.alice.rs.response.dto.Facebook;
import com.cwp.alice.rs.response.dto.FollowupEvent;
import com.cwp.alice.rs.response.dto.Google;
import com.cwp.alice.rs.response.dto.Item;
import com.cwp.alice.rs.response.dto.Messages;
import com.cwp.alice.rs.response.dto.Parameters;
import com.cwp.alice.rs.response.dto.Parameters2;
import com.cwp.alice.rs.response.dto.ResponseRootObject;
import com.cwp.alice.rs.response.dto.RichResponse;
import com.cwp.alice.rs.response.dto.SimpleResponse;
import com.cwp.alice.rs.response.dto.Slack;

@RestController
public class DialogFlowConversationController {
	private static final Logger logger = Logger.getLogger(CaseWorkerPortalController.class);
	public static final Logger errorLogger = Logger.getLogger(GenericConstants.LOGGER_ERROR_NAME);
	
	@RequestMapping(value = GenericConstants.DIALOGFLOW_CONVERSATION_WEBHOOK, method = RequestMethod.POST,
			consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseRootObject processCaseCreation(@RequestBody RequestRootObject requestRootObject) {
		ResponseRootObject responseRootObject = new ResponseRootObject();

		try {
			responseRootObject.setSpeech("this text is spoken out loud if the platform supports voice interactions");
			responseRootObject.setDisplayText("this text is displayed visually");
			
			Messages messages = new Messages();
			messages.setType(1);
			messages.setTitle("card title");
			messages.setSubtitle("card text");
			messages.setImageUrl("https://assistant.google.com/static/images/molecule/Molecule-Formation-stop.png");
			responseRootObject.setMessages(messages);
			
			Data data = new Data();
			Google google = new Google();
			google.setExpectUserResponse(true);
			RichResponse richResponse = new RichResponse();
			ArrayList<Item> listOfItems = new ArrayList<>();
			Item item = new Item();
			SimpleResponse simpleResponse = new SimpleResponse();
			simpleResponse.setTextToSpeech("this is a simple response");
			item.setSimpleResponse(simpleResponse);
			listOfItems.add(item);
			richResponse.setItems(listOfItems);
			google.setRichResponse(richResponse);
			data.setGoogle(google);
			
			Facebook facebook = new Facebook();
			facebook.setText("Hello, Facebook!");
			data.setFacebook(facebook);
			
			Slack slack = new Slack();
			slack.setText("This is a text response for Slack.");
			data.setSlack(slack);
			responseRootObject.setData(data);
			
			ArrayList<ContextOut> listOfContextOuts = new ArrayList<>();
			ContextOut contextOut = new ContextOut();
			contextOut.setName("context name");
			contextOut.setLifespan(5);
			Parameters parameters = new Parameters();
			parameters.setParam("param value");
			contextOut.setParameters(parameters);
			listOfContextOuts.add(contextOut);
			responseRootObject.setContextOut(listOfContextOuts);
			
			responseRootObject.setSource("cws.openshift.com");
			
			FollowupEvent followupEvent = new FollowupEvent();
			followupEvent.setName("event name");
			Parameters2 followUpParameters = new Parameters2();
			followUpParameters.setParam("param value");
			followupEvent.setParameters(followUpParameters);
			responseRootObject.setFollowupEvent(followupEvent);
			
			logger.error("Successfully created object");
			
		} catch (Exception e) {
			errorLogger.error("Classname: CwpDashboardController. Error in Case Creation: " + e);
			logger.error("Error in Case Creation: " + e);
			
		}
		
		return responseRootObject;
	}
}
