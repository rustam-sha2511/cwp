/**
 * 
 */
package com.cwp.alice.service;

import java.util.List;

import com.cwp.alice.model.CwAppointments;

/**
 * @author lugupta
 *
 */
public interface DialogFlowConversationService {

	List<CwAppointments> getCwAppointments(String cwId) throws Exception;

}
