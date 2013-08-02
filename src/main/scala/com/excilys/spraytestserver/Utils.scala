package com.excilys.spraytestserver

import spray.http._

object Utils {

  case class Expires(date: DateTime) extends HttpHeader{
    def name: String = "Expires"
    def value: String = date.toRfc1123DateTimeString
    def lowercaseName: String = name.toLowerCase
    def render[R <: Rendering](r: R): r.type = r ~~ name ~~ ':' ~~ ' ' ~~ value
  }

  val htmlIpsum = {
    <html>
    <form action="#" method="post">
      <div>
        <label for="name">Text Input:</label>
        <input type="text" name="name" id="name" value="" tabindex="1" />
      </div>

      <div>
        <h4>Radio Button Choice</h4>

        <label for="radio-choice-1">Choice 1</label>
        <input type="radio" name="radio-choice-1" id="radio-choice-1" tabindex="2" value="choice-1" />

        <label for="radio-choice-2">Choice 2</label>
        <input type="radio" name="radio-choice-2" id="radio-choice-2" tabindex="3" value="choice-2" />
      </div>

      <div>
        <label for="select-choice">Select Dropdown Choice:</label>
        <select name="select-choice" id="select-choice">
          <option value="Choice 1">Choice 1</option>
          <option value="Choice 2">Choice 2</option>
          <option value="Choice 3">Choice 3</option>
        </select>
      </div>

      <div>
        <label for="textarea">Textarea:</label>
        <textarea cols="40" rows="8" name="textarea" id="textarea"></textarea>
      </div>

      <div>
        <label for="checkbox">Checkbox:</label>
        <input type="checkbox" name="checkbox" id="checkbox" />
      </div>

      <div>
        <input type="submit" value="Submit" />
      </div>
    </form>
    </html>
  }

  val soapXml =     "<?xml version='1.0' encoding='UTF-8' ?>\n" + {
      <SOAP-ENV:Envelope
      xmlns:xsi="http://www.w3.org/1999/XMLSchema-instance"
      xmlns:xsd="http://www.w3.org/1999/XMLSchema"
      xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
        <SOAP-ENV:Body>
          <ns1:getEmployeeDetailsResponse
          xmlns:ns1="urn:MySoapServices"
          SOAP-ENV:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
            <return xsi:type="ns1:EmployeeContactDetail">
              <employeeName xsi:type="xsd:string">Bill Posters</employeeName>
              <phoneNumber xsi:type="xsd:string">+1-212-7370194</phoneNumber>
              <tempPhoneNumber
              xmlns:ns2="http://schemas.xmlsoap.org/soap/encoding/"
              xsi:type="ns2:Array"
              ns2:arrayType="ns1:TemporaryPhoneNumber[3]">
                <item xsi:type="ns1:TemporaryPhoneNumber">
                  <startDate xsi:type="xsd:int">37060</startDate>
                  <endDate xsi:type="xsd:int">37064</endDate>
                  <phoneNumber xsi:type="xsd:string">+1-515-2887505</phoneNumber>
                </item>
                <item xsi:type="ns1:TemporaryPhoneNumber">
                  <startDate xsi:type="xsd:int">37074</startDate>
                  <endDate xsi:type="xsd:int">37078</endDate>
                  <phoneNumber xsi:type="xsd:string">+1-516-2890033</phoneNumber>
                </item>
                <item xsi:type="ns1:TemporaryPhoneNumber">
                  <startDate xsi:type="xsd:int">37088</startDate>
                  <endDate xsi:type="xsd:int">37092</endDate>
                  <phoneNumber xsi:type="xsd:string">+1-212-7376609</phoneNumber>
                </item>
              </tempPhoneNumber>
            </return>
          </ns1:getEmployeeDetailsResponse>
        </SOAP-ENV:Body>
      </SOAP-ENV:Envelope>
  }.toString


  val jsonObject = {
    "{ \"Image\": "+
      "{ \"Width\":  800, \"Height\": 600, \"Title\":  \"View from 15th Floor\", \"Thumbnail\": " +
        "{\"Url\":    \"http://www.example.com/image/481989943\", \"Height\": 125, \"Width\":  null }, \"IDs\": [116, 943, 234, 38793]" +
      "}" +
    "}"
  }

  val jsonArray = {
    "[" +
      "{" +
        "\"precision\": \"zip\", \"Latitude\":  37.7668, \"Longitude\": -122.3959, \"Address\": null ,\"City\": \"SAN FRANCISCO\"," +
        "\"State\": \"CA\",\"Zip\":\"94107\", \"Country\":\"US\" " +
      "}," +
      "{" +
      "\"precision\": \"zip\", \"Latitude\":  37.371991, \"Longitude\": -122.026020, \"Address\": \"\" ,\"City\": \"SUNNYVALE\"," +
      "\"State\": \"CA\",\"Zip\":\"94085\", \"Country\":\"US\" " +
      "}" +
    "]"
  }
}
