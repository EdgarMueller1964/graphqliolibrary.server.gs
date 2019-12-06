package com.thinkenterprise.graphqlio.samples;

import java.sql.Date;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

public class Route {

	private String flightNumber = null;
	private String departure = null;
	private String destination = null;
	private String disabled = null;
	private UUID signature = null;
	private Date bookingDate = null;

	public Route() {
		super();
		this.flightNumber = "";
	}

	public Route(String jsonStr) throws JSONException {
		super();
		JSONObject flightObj = new JSONObject(jsonStr);
		this.flightNumber = flightObj.getString("flightNumber");
		this.departure = flightObj.getString("departure");
		this.destination = flightObj.getString("destination");
	}

	public Route(String flightNumber, String departure, String destination) {
		super();
		this.flightNumber = flightNumber;
		this.departure = departure;
		this.destination = destination;
	}

	public String getFlightNumber() {
		return flightNumber;
	}

	public void setFlightNumber(String number) {
		this.flightNumber = number;
	}

	public String getDeparture() {
		return departure;
	}

	public void setDeparture(String departure) {
		this.departure = departure;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getDisabled() {
		return disabled;
	}

	public void setDisabled(String disabled) {
		this.disabled = disabled;
	}

	public UUID getSignature() {
		return signature;
	}

	public void setSignature(UUID signature) {
		this.signature = signature;
	}

	public Date getBookingDate() {
		return bookingDate;
	}

	public void setBookingDate(Date bookingDate) {
		this.bookingDate = bookingDate;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Route)) {
			return false;

		}

		Route route = (Route) obj;

		if ((this.flightNumber != null && route.getFlightNumber() == null)
				|| (this.departure != null && route.getDeparture() == null)
				|| (this.destination != null && route.getDestination() == null)) {
			return false;

		} else if ((this.flightNumber == null && route.getFlightNumber() != null)
				|| (this.departure == null && route.getDeparture() != null)
				|| (this.destination == null && route.getDestination() != null)) {
			return false;

		} else if ((this.flightNumber != null && this.flightNumber.equals(route.getFlightNumber()))
				&& (this.departure != null && this.departure.equals(route.getDeparture()))
				&& (this.destination != null && this.destination.equals(route.getDestination()))) {
			return true;

		} else {
			return false;
		}
	}

}
