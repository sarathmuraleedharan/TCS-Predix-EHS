package com.ge.predix.solsvc.experience.datasource.datagrid.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ge.predix.entity.timeseries.datapoints.queryresponse.DatapointsResponse;
import com.ge.predix.solsvc.experience.datasource.handlers.HygieneDatasourceHandler;
import com.ge.predix.solsvc.experience.datasource.handlers.HygieneHandler;
import com.ge.predix.solsvc.experience.datasource.handlers.utils.TimeSeriesHygieneParser.ResponseObject;
import com.ge.predix.solsvc.experience.datasource.handlers.utils.TimeSeriesHygieneParser.ResponseObjectCollections;
import com.ge.predix.solsvc.experience.datasource.handlers.utils.TimeSeriesHygieneParser;

@Consumes({ "application/json" })
@Produces({ "application/json" })
@Path("/hygiene")
@Component(value = "hygieneService")
public class HygieneService {
	@Autowired
	HygieneDatasourceHandler hygieneDatasourceHandler;

	@Autowired
	TimeSeriesHygieneParser timeSeriesHygieneParser;

	@Autowired
	private HygieneHandler hygieneHandler;

	@GET
	@Path("/last24Hrs")
	public ResponseEntity<ArrayList<Object>> last24Hrs() {
		ArrayList<Object> list = hygieneHandler.getlast24HrsData();
		return new ResponseEntity<ArrayList<Object>>(list, HttpStatus.OK);
	}

	@GET
	@Path("/last20Sec")
	public ResponseEntity<Object> last20Sec(@HeaderParam(value = "authorization") String authorization) throws JsonProcessingException {
		Long twentySec = 20l * 1000l; // 20sec in ms
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		Long currentTime = calendar.getTimeInMillis();
		String start_time = String.valueOf(currentTime - twentySec);
		String end_time = String.valueOf(currentTime);

		// log.info("id : " + assetName);
		// log.info("authorization : " + authorization);
		// log.info("start_time : " + start_time);
		// log.info("end_time : " + end_time);

		DatapointsResponse datapointsResponse = hygieneDatasourceHandler.getGeographyResponse(authorization, start_time, end_time);
		List<ResponseObjectCollections> list = timeSeriesHygieneParser.parse(datapointsResponse);
		return new ResponseEntity<Object>(timeSeriesHygieneParser.parseForResponse(list), HttpStatus.OK);
	}

	@GET
	@Path("/last10Min")
	public ResponseEntity<Object> last10Min(@HeaderParam(value = "authorization") String authorization) throws Throwable {
		Long twentyFourHours = 10l * 60l * 1000l; // 10 min in ms
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		Long currentTime = calendar.getTimeInMillis();
		String start_time = String.valueOf(currentTime - twentyFourHours);
		String end_time = String.valueOf(currentTime);

		DatapointsResponse datapointsResponse = hygieneDatasourceHandler.getGeographyResponse(authorization, start_time, end_time);
		List<ResponseObjectCollections> list = timeSeriesHygieneParser.parse(datapointsResponse);
		return new ResponseEntity<Object>(timeSeriesHygieneParser.parseForResponse(list), HttpStatus.OK);
	}

	@GET
	@Path("/dashboardValues")
	public ResponseEntity<ArrayList<Object>> dashboardValues() {
		ArrayList<Object> list = hygieneHandler.dashboardValues();
		return new ResponseEntity<ArrayList<Object>>(list, HttpStatus.OK);
	}
}
