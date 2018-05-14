/**
 * 
 */
package com.cwp.alice.service.impl;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cwp.alice.dao.CaseWorkerPortalDAO;
import com.cwp.alice.model.CwAppointments;
import com.cwp.alice.service.DialogFlowConversationService;
import com.cwp.alice.util.DateUtil;

/**
 * @author lugupta
 *
 */
@Service
public class DialogFlowConversationServiceImpl implements DialogFlowConversationService {

	@Autowired
	CaseWorkerPortalDAO cwpDAO;

	@Override
	public List<CwAppointments> getCwAppointments(String cwId) throws Exception {
		List<CwAppointments> cwAppointmentsList = cwpDAO.getCwAppointments(Integer.valueOf(cwId));
		String todayDate = DateUtil.getDateInMMDDYYYYString(new Date());

		List<CwAppointments> filteredAppointments = new ArrayList<>();
		for (CwAppointments obj : cwAppointmentsList) {
			LocalTime lt = LocalTime.parse(obj.getTime());
			if (!obj.getDate().equalsIgnoreCase(todayDate) || !lt.isAfter(LocalTime.now(ZoneId.of("Asia/Kolkata")))) {
				filteredAppointments.add(obj);
			}
		}

		cwAppointmentsList.removeAll(filteredAppointments);

		return cwAppointmentsList;
	}
}