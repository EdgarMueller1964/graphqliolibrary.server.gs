/*
**  Design and Development by msg Applied Technology Research
**  Copyright (c) 2019-2020 msg systems ag (http://www.msg-systems.com/)
**  All Rights Reserved.
** 
**  Permission is hereby granted, free of charge, to any person obtaining
**  a copy of this software and associated documentation files (the
**  "Software"), to deal in the Software without restriction, including
**  without limitation the rights to use, copy, modify, merge, publish,
**  distribute, sublicense, and/or sell copies of the Software, and to
**  permit persons to whom the Software is furnished to do so, subject to
**  the following conditions:
**
**  The above copyright notice and this permission notice shall be included
**  in all copies or substantial portions of the Software.
**
**  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
**  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
**  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
**  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
**  CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
**  TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
**  SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package com.thinkenterprise.graphqlio.samples;

import java.sql.Date;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class used to process any incoming message sent by clients via WebSocket
 * supports subprotocols (CBOR, MsgPack, Text)
 * triggers process to indicate outdating queries and notifies clients
 *
 * @author Michael Schäfer
 * @author Torsten Kühnert
 */

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
