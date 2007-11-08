/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.formentry.web.dynamic;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PersonAttribute;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.formentry.FormEntryService;
import org.openmrs.propertyeditor.LocationEditor;
import org.openmrs.propertyeditor.UserEditor;
import org.openmrs.reporting.PatientSet;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class BatchFormEntryController extends SimpleFormController {

	/** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());

	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
	    binder.registerCustomEditor(java.lang.Integer.class,
                new CustomNumberEditor(java.lang.Integer.class, true));
	    binder.registerCustomEditor(java.lang.Double.class,
                new CustomNumberEditor(java.lang.Double.class, true));
	    binder.registerCustomEditor(Location.class, new LocationEditor());
	    binder.registerCustomEditor(User.class, new UserEditor());	    
	}
    
    protected BatchFormEntryModel formBackingObject(HttpServletRequest request) throws Exception {
    	BatchFormEntryModel batchForm = new BatchFormEntryModel();

    	PatientSet ps = Context.getPatientSetService().getMyPatientSet();
    	if (ps == null || ps.size() == 0)
    		throw new RuntimeException("You need a patient set first");
 
    	batchForm.setPatientSet(ps);
    	Form form = Context.getFormService().getForm(Integer.valueOf(request.getParameter("formId")));
    	batchForm.setForm(form);
    	batchForm.getFieldsFromForm();
    	
    	if (request.getParameter("locationId") != null) {
    		Integer locationId = Integer.valueOf(request.getParameter("locationId"));
    		Location l = Context.getEncounterService().getLocation(locationId);
    		batchForm.setLocation(l);
    	} else {
    		// TODO: check all patients for their assigned location / last encounter location, and default to the most common one, or else none
    		Patient p = Context.getPatientService().getPatient(ps.getPatientIds().iterator().next());
    		PersonAttribute attr = p.getAttribute("Health Center");
    		if (attr != null) {
	    		String value = attr.getValue();
	    		if (value != null && !value.equals(""))
	    			batchForm.setLocation(new Location(Integer.valueOf(value)));
    		}
    	}
    	
    	request.getParameter("providerId");
    	if (request.getParameter("providerId") != null) {
    		Integer providerId = Integer.valueOf(request.getParameter("providerId"));
    		User u = Context.getUserService().getUser(providerId);
    		batchForm.setProvider(u);
    	} else {
    		FormEntryService fes = (FormEntryService)Context.getService(FormEntryService.class);
    		User u = fes.getUserByUsername("Unknown");
    		batchForm.setProvider(u);
    	}
    	
		return batchForm;
    }
    
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
    		Object comm, BindException errors) throws Exception {

    	throw new RuntimeException("Not Implemented");
    }

	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected Map<String, Object> referenceData(HttpServletRequest request) throws Exception {
		Map<String, Object> extraData = new HashMap<String, Object>();
		
		String datePattern = OpenmrsConstants.OPENMRS_LOCALE_DATE_PATTERNS().get(Context.getLocale().toString().toLowerCase());

		extraData.put("datePattern", datePattern);
		
		return extraData;
	}
    
}
